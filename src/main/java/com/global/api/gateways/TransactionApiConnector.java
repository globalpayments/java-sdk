package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.ServiceEndpoints;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.transactionApi.TransactionApiAuthRequestBuilder;
import com.global.api.entities.transactionApi.TransactionApiManagementRequestBuilder;
import com.global.api.entities.transactionApi.TransactionApiRequest;
import com.global.api.mapping.TransactionApiMapping;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.serviceConfigs.TransactionApiConfig;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import org.apache.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;

import static com.global.api.logging.PrettyLogger.toPrettyJson;

public class TransactionApiConnector extends RestGateway implements IPaymentGateway {
    private final Logger logger = Logger.getLogger("TransactionApiConnector.class");
    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private static final String TRANSACTION_API_VERSION = "2021-04-08";
    private static final String TRANSACTION_API_PARTNER_APP_NAME = "mobile_sdk";
    private static final String TRANSACTION_API_PARTNER_APP_VERSION = "1";
    private String token;
    @Getter
    private final TransactionApiConfig apiConfig;

    public TransactionApiConnector(TransactionApiConfig config) {
        this.apiConfig = config;

        setServiceUrl(apiConfig.getEnvironment().equals(Environment.PRODUCTION) ? ServiceEndpoints.TRANSACTION_API_PRODUCTION.getValue() : ServiceEndpoints.TRANSACTION_API_TEST.getValue());
        setEnableLogging(apiConfig.isEnableLogging());

        headers.put(HttpHeaders.ACCEPT, "application/json");
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        headers.put("X-GP-Version", TRANSACTION_API_VERSION);
        headers.put("X-GP-Api-Key", apiConfig.getAppKey());
        headers.put("X-GP-Partner-App-Name", TRANSACTION_API_PARTNER_APP_NAME);
        headers.put("X-GP-Partner-App-Version", TRANSACTION_API_PARTNER_APP_VERSION);

        dynamicHeaders = apiConfig.getDynamicHeaders();
    }

    private void generateToken() throws NoSuchAlgorithmException, InvalidKeyException {
        String accountCredential = apiConfig.getAccountCredential();
        String region = apiConfig.getRegion().getValue();
        String apiSecret = apiConfig.getAppSecret();

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
                .set("account_credential", accountCredential)
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

        this.token = jwtHeaderATBase64 + "." + jwtPayloadATBase64 +
                "." + hashSignature;
        log("Generated AuthtokenV2: " + this.token);

    }

    public String doTransaction(TransactionApiRequest.HttpMethod verb, String endpoint, String data, HashMap<String, String> queryStringParams) throws GatewayException {
        try {
            generateToken();
            headers.put("Authorization", "AuthToken " + token);
            return super.doTransaction(verb.getValue(), endpoint, data, queryStringParams);
        } catch (GatewayException ex) {
            // Handling error response messages
            if(ex.getResponseCode() != null
                    && (ex.getResponseCode().equals("471")
                    || ex.getResponseCode().equals("470")
                    || ex.getResponseCode().equals("404")
                    || ex.getResponseCode().equals("400")
                    || ex.getResponseCode().equals("403"))){
                log("Response: " + toPrettyJson(ex.getResponseText()));
                return ex.getResponseText();
            }
            throw ex;
        } catch (Exception ex) {
            throw new GatewayException(ex.getMessage());
        }
    }

    @Override
    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        try {
            TransactionApiRequest request = TransactionApiAuthRequestBuilder.buildRequest(builder, this);
            if (request != null) {
                String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams());
                return TransactionApiMapping.mapResponse(response);
            }
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }
        return null;
    }

    @Override
    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        try {
            TransactionApiRequest request = TransactionApiManagementRequestBuilder.buildRequest(builder, this);
            if (request != null) {
                String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams());
                return TransactionApiMapping.mapResponse(response);
            }
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage());
        }
        return null;
    }

    @Override
    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        return null;
    }

    @Override
    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        return null;
    }

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    private void log(String message){
        if(apiConfig.isEnableLogging()){
            logger.info(message);
        }
    }
}
