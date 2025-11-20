package com.global.api.entities.gpApi;

import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.PorticoTokenConfig;
import com.global.api.gateways.IAccessTokenProvider;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class GpApiSessionInfo implements IAccessTokenProvider {

    /**
     * A unique string created using the nonce and app-key.
     * This value is used to further authenticate the request.
     * Created as follows - SHA512(NONCE + APP-KEY).
     **/
    public static String generateSecret(String nonce, String appKey) throws GatewayException {
        String generatedPassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(nonce.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(appKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new GatewayException("Algorithm not available in the environment", e);
        }
        return generatedPassword;
    }

    public GpApiRequest signIn(String appId, String appKey, int secondsToExpire, IntervalToExpire intervalToExpire, String[] permissions, PorticoTokenConfig porticoTokenConfig) throws GatewayException {
        String nonce = DateTime.now().toString("MM/dd/yyyy hh:mm:ss.SSS a");
        JsonDoc request;

        if (porticoTokenConfig != null) {
            ArrayList<HashMap<String, String>> credentials = buildCredentials(porticoTokenConfig);
            request = new JsonDoc()
                    .set("credentials", credentials)
                    .set("grant_type", "client_credentials");
            if (appId != null && !appId.isEmpty()) {
                request.set("app_id", appId);
            }
        } else {
            request = new JsonDoc()
                    .set("app_id", appId)
                    .set("nonce", nonce)
                    .set("grant_type", "client_credentials")
                    .set("secret", generateSecret(nonce, appKey))
                    .set("permissions", permissions);
            if (secondsToExpire != 0) {
                request.set("seconds_to_expire", secondsToExpire);
            }
            if (intervalToExpire != null) {
                request.set("interval_to_expire", intervalToExpire);
            }
        }

        return (GpApiRequest) new GpApiRequest()
                .setVerb(GpApiRequest.HttpMethod.Post)
                .setEndpoint(GpApiRequest.ACCESS_TOKEN_ENDPOINT)
                .setRequestBody(request.toString());
    }

    private ArrayList<HashMap<String, String>> buildCredentials(PorticoTokenConfig config) {
        ArrayList<HashMap<String, String>> credentials = new ArrayList<>();
        boolean hasDevice = config.getDeviceId() != 0;
        boolean hasSite = config.getSiteId() != 0;
        boolean hasLicense = config.getLicenseId() != 0;
        boolean hasUsername = config.getUsername() != null && !config.getUsername().isEmpty();
        boolean hasPassword = config.getPassword() != null && !config.getPassword().isEmpty();
        boolean hasApiKey = config.getSecretApiKey() != null && !config.getSecretApiKey().isEmpty();

        if (hasDevice && hasSite && hasLicense && hasUsername && hasPassword) {
            credentials.add(createCredential("device_id", String.valueOf(config.getDeviceId())));
            credentials.add(createCredential("site_id", String.valueOf(config.getSiteId())));
            credentials.add(createCredential("license_id", String.valueOf(config.getLicenseId())));
            credentials.add(createCredential("username", config.getUsername()));
            credentials.add(createCredential("password", config.getPassword()));
            if (hasApiKey) {
                credentials.add(createCredential("apikey", config.getSecretApiKey()));
            }
        } else if (hasApiKey) {
            credentials.add(createCredential("apikey", config.getSecretApiKey()));
        }
        return credentials;
    }

    private HashMap<String, String> createCredential(String name, String value) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }

    public GpApiRequest signOut() throws GatewayException {
        return null;
    }
}
