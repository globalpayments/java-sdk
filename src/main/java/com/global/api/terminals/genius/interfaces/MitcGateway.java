package com.global.api.terminals.genius.interfaces;

import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.GatewayResponse;
import com.global.api.gateways.RestGateway;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.genius.enums.MitcRequestType;
import com.global.api.terminals.genius.request.GeniusMitcRequest;
import com.global.api.terminals.genius.responses.MitcResponse;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import org.apache.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Logger;

public class MitcGateway extends RestGateway {
    private final Logger logger = Logger.getLogger("TransactionApiConnector.class");
    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    @Getter
    private final ConnectionConfig config;
    private final String accountCredential;
    private static final String TRANSACTION_API_VERSION = "2021-04-08";
    private String requestId;

    public String targetDevice;

    public MitcGateway(ITerminalConfiguration settings) {
        this.config = (ConnectionConfig) settings;
        targetDevice = config.getGeniusMitcConfig().getTargetDevice();

        setServiceUrl(
                config.getEnvironment().equals(Environment.PRODUCTION)
                        ? ServiceEndpoints.GENIUS_MITC_PRODUCTION.getValue()
                        : ServiceEndpoints.GENIUS_MITC_TEST.getValue()
        );

        setEnableLogging(config.isEnableLogging());
        accountCredential
                = this.config.getGeniusMitcConfig().getXWebId() + ":"
                + this.config.getGeniusMitcConfig().getTerminalId() + ":"
                + this.config.getGeniusMitcConfig().getAuthKey();


        headers.put("X-GP-Version", TRANSACTION_API_VERSION);
        headers.put("X-GP-Api-Key", config.getGeniusMitcConfig().getApiKey());
        headers.put("X-GP-Target-Device", config.getGeniusMitcConfig().getTargetDevice());
        headers.put("X-GP-Partner-App-Name", config.getGeniusMitcConfig().getAppName());
        headers.put("X-GP-Partner-App-Version", config.getGeniusMitcConfig().getAppVersion());

        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
    }

    private String generateToken() throws NoSuchAlgorithmException, InvalidKeyException {
        String region = this.config.getGeniusMitcConfig().getRegion();
        String apiSecret = this.config.getGeniusMitcConfig().getApiSecret();

        // Creating JSON object for Header , json-simple JAR is required.
        JsonDoc jwtHeaderObj = new JsonDoc();
        jwtHeaderObj
                .set("alg", "HS256")
                .set("typ", "JWT");

        // Base64 encoding of Header
        String jwtHeaderATBase64 = Base64.getUrlEncoder().encodeToString(jwtHeaderObj.toString().getBytes(UTF8_CHARSET));

        // Creating JSON object for Payload
        JsonDoc jwtPayloadObj = new JsonDoc();
        jwtPayloadObj
                .set("type", "AuthTokenV2")
                .set("account_credential", this.accountCredential)
                .set("region", region)
                .set("ts", System.currentTimeMillis());

        // Base64 encoding of Payload
        String jwtPayloadATBase64 =
                Base64
                        .getUrlEncoder()
                        .encodeToString(jwtPayloadObj.toString().getBytes(UTF8_CHARSET));

        // Concatenating encoded Header and Payload with "."
        String jwtMessage = jwtHeaderATBase64 + "." + jwtPayloadATBase64;

        // Create Signature using HMAC-SH256 algorithm and ApiSecret as the secret
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new
                SecretKeySpec(String.valueOf(apiSecret).getBytes(), "HmacSHA256");
        sha256HMAC.init(secretKey);
        String hashSignature = Base64.getUrlEncoder()
                .encodeToString(sha256HMAC.doFinal(jwtMessage.getBytes(UTF8_CHARSET)));

        /**
         * Create the JWT AuthToken by concatenating Base64 URL encoded header,
         * payload and signature
         */

        String token = jwtHeaderATBase64 + "." + jwtPayloadATBase64 +
                "." + hashSignature;
        log("Generated Auth token V2: " + token);

        return token;
    }

    public MitcResponse doTransaction(
            GeniusMitcRequest.HttpMethod verb,
            String endpoint,
            String requestBody,
            MitcRequestType requestType

    ) throws GatewayException {
        try {
            headers.put("Authorization", "AuthToken " + generateToken());
            headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(StringUtils.isNullOrEmpty(requestBody) ? 0 : requestBody.length()));
            headers.put("X-GP-Request-Id", this.requestId);
            headers.put("X-GP_Version", "2021-04-08");

            GatewayResponse response =super.sendRequest(verb.getValue(), endpoint, requestBody, null);
            return new MitcResponse(response.getStatusCode(),"",response.getRawResponse());
        } catch (GatewayException ex) {
            // Handling error response messages
            if (ex.getResponseCode() != null
                    && (ex.getResponseCode().equals("471")
                    || ex.getResponseCode().equals("470")
                    || ex.getResponseCode().equals("404")
                    || ex.getResponseCode().equals("400")
                    || ex.getResponseCode().equals("403")
                    || ex.getResponseCode().equals("401"))) {
                log("Response: " + ex.getResponseText());
            }
            throw ex;
        } catch (Exception ex) {
            throw new GatewayException(ex.getMessage());
        }
    }

    public void log(String message) {
        if (config.isEnableLogging()) {
            logger.info(message);
        }
    }

}
