/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.check3dsversion;

import com.example.jsonreader.JsonReaderHelper;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.Secure3dService;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.math.BigDecimal;

class ThreeDSVersionChecker {


    private final JsonReaderHelper jsonReaderHelper = new JsonReaderHelper();

    JsonObject check(BufferedReader reader) {

        System.out.println();
        System.out.println("*** 02 *** ThreeDSVersionChecker");
        System.out.println();

        JsonObject requestData = jsonReaderHelper.getJsonObject(reader);

        CreditCardData paymentMethod = new CreditCardData();
        paymentMethod.setToken(requestData.get("tokenResponse").getAsString());

        ThreeDSecure threeDSecureData;

        try {
            threeDSecureData = Secure3dService.checkEnrollment(paymentMethod)
                    // TODO this hard coded values, check where from we should get them
                    .withAmount(new BigDecimal("100")).withCurrency("EUR").execute();
        } catch (ApiException e) {
            // TODO: add your error handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }

        // if enrolled, available response data
        // serverTransactionId = threeDSecureData.getServerTransactionId(); //
        // AUT_90092646-e424-452e-bbf1-09d2daf8af05
        // dsStartProtocolVersion = threeDSecureData.getDirectoryServerStartVersion();
        // // 2.1.0
        // dsEndProtocolVersion = threeDSecureData.getDirectoryServerEndVersion(); //
        // 2.1.0
        // acsStartProtocolVersion = threeDSecureData.getAcsStartVersion(); // 2.1.0
        // acsEndProtocolVersion = threeDSecureData.getAcsEndVersion(); // 2.1.0
        // methodUrl = threeDSecureData.getIssuerAcsUrl(); //
        // https://www.acsurl.com/method
        // encodedMethodData = threeDSecureData.getPayerAuthenticationRequest(); //
        // Base64 encoded string

        JsonObject response = new JsonObject();

        response.addProperty("enrolled", threeDSecureData.getEnrolledStatus());
        response.addProperty("version", threeDSecureData.getVersion().getValue());
        // TODO "messageVersion" is evaluated in main.js, so it has to be set
        response.addProperty("messageVersion", threeDSecureData.getVersion().name());
        response.addProperty("status", threeDSecureData.getStatus());
        response.addProperty("liabilityShift", threeDSecureData.getLiabilityShift());
        response.addProperty("serverTransactionId", threeDSecureData.getServerTransactionId());
        response.addProperty("sessionDataFieldName", threeDSecureData.getSessionDataFieldName());

        if (!response.get("enrolled").getAsString().equals("ENROLLED")) {
            /*
             * TODO: do not proceed to authorization and ask the customer to try another
             * card instead
             */
            throw new RuntimeException("just fail fast in this example");
        }

        if (response.get("version").getAsString().equals(Secure3dVersion.TWO.getValue())) {
            response.addProperty("methodUrl", threeDSecureData.getIssuerAcsUrl());
            response.addProperty("methodData", threeDSecureData.getPayerAuthenticationRequest());
            response.addProperty("messageType", threeDSecureData.getMessageType());

            /*
             * TODO: pass the Enrolled status, Method URL and Encoded Method Data (if
             * supported) to the client-side Sample string:
             * {"enrolled":"ENROLLED","version":"TWO","status":"AVAILABLE","liabilityShift":
             * null,"serverTransactionId":"AUT_88f7f878-b18d-4085-a970-46e3b117bea5",
             * "sessionDataFieldName":"threeDSSessionData","methodUrl":
             * "https:\/\/test.portal.gpwebpay.com\/pay-sim\/sim\/acs","methodData":
             * "ewogICJ0aHJlZURTU2VydmVyVHJhbnNJRCIgOiAiODhmN2Y4NzgtYjE4ZC00MDg1LWE5NzAtNDZlM2IxMTdiZWE1IiwKICAidGhyZWVEU01ldGhvZE5vdGlmaWNhdGlvblVSTCIgOiAiaHR0cDovL3NhbXBsZWNvZGUubG9jYWxob3N0LmNvbTo4MDgwL2V4YW1wbGVzMy9UaHJlZURTZWN1cmUvTWV0aG9kTm90aWZpY2F0aW9uLyIKfQ"
             * ,"messageType":"creq"}
             */

            // Use your desired JAVA HTTP framework the response object with 'Content-Type:
            // application/json'
        }

        return response;
    }

}