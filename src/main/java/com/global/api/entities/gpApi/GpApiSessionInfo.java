package com.global.api.entities.gpApi;

import com.global.api.entities.enums.IntervalToExpire;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.GpApiConnector;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GpApiSessionInfo {

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

    public static GpApiRequest signIn(String appId, String appKey, int secondsToExpire, IntervalToExpire intervalToExpire, String[] permissions) throws GatewayException {
        String nonce = DateTime.now().toString(GpApiConnector.DATE_TIME_DTF);

        JsonDoc request =
                new JsonDoc()
                        .set("app_id", appId)
                        .set("nonce", nonce)
                        .set("grant_type", "client_credentials")
                        .set("secret", generateSecret(nonce, appKey))
                        .set("permissions", permissions);

        if(secondsToExpire != 0) {
            request.set("seconds_to_expire", secondsToExpire);
        }

        if(intervalToExpire != null) {
            request.set("interval_to_expire", intervalToExpire);
        }

        return
                new GpApiRequest()
                        .setVerb(GpApiRequest.HttpMethod.Post)
                        .setEndpoint("/accesstoken")
                        .setRequestBody(request.toString());
    }

    public static GpApiRequest signOut() throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException("SignOut not implemented");

        //return new PayrollRequest
        //{
        //    Endpoint = "/api/pos/session/signout"
        //};
    }

}
