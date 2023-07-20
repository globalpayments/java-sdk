/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.initiateauthentication;

import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.MethodUrlCompletion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.Secure3dService;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

class AuthenticationHandler {

    private final EnumHelper enumHelper = new EnumHelper();

    Map<String, Object> doAuthentication(JsonObject requestData) {

        System.out.println();
        System.out.println("*** 04 *** AuthenticationHandler");
        System.out.println();

        // TODO: Add the customer's billing address
        Address billingAddress = new Address();
        billingAddress.setStreetAddress1("Apartment 852");
        billingAddress.setStreetAddress2("Complex 741");
        billingAddress.setStreetAddress3("Unit 4");
        billingAddress.setCity("Chicago");
        billingAddress.setState("IL");
        billingAddress.setPostalCode("50001");
        billingAddress.setCountryCode("840");

        // TODO: Add the customer's shipping address
        Address shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Flat 456");
        shippingAddress.setStreetAddress2("House 789");
        shippingAddress.setStreetAddress3("Basement Flat");
        shippingAddress.setCity("Halifax");
        shippingAddress.setPostalCode("W5 9HR");
        shippingAddress.setCountryCode("826");

        // TODO: Add captured browser data from the client-side and server-side
        BrowserData browserData = new BrowserData();
        browserData
                .setAcceptHeader("text/html,application/xhtml+xml,application/xml;q=9,image/webp,img/apng,*/*;q=0.8");
        browserData.setColorDepth(
                enumHelper.getColorDepthByName(requestData.get("browserData").getAsJsonObject().get("colorDepth").getAsString()));
        browserData.setIpAddress("123.123.123.123");
        JsonObject bData = requestData.get("browserData").getAsJsonObject();
        browserData.setJavaEnabled(bData.get("javaEnabled").getAsBoolean());
        browserData.setLanguage(bData.get("language").getAsString());
        browserData.setScreenHeight(bData.get("screenHeight").getAsInt());
        browserData.setScreenWidth(bData.get("screenWidth").getAsInt());
        browserData.setChallengeWindowSize(enumHelper.getChallengeWindowSizeByName(
                requestData.get("challengeWindow").getAsJsonObject().get("windowSize").getAsString()));
        browserData.setTimezone(bData.get("timezoneOffset").getAsString());
        browserData.setUserAgent(bData.get("userAgent").getAsString());

        CreditCardData paymentMethod = new CreditCardData();
        paymentMethod.setToken(requestData.get("tokenResponse").getAsString());

        ThreeDSecure threeDSecureData = new ThreeDSecure();
        try {

            threeDSecureData
                    .setServerTransactionId(requestData.get("serverTransactionId").getAsString());
            MethodUrlCompletion methodUrlCompletion = MethodUrlCompletion.Yes;

            JsonObject oData = requestData.get("order").getAsJsonObject();

            threeDSecureData = Secure3dService.initiateAuthentication(paymentMethod, threeDSecureData)
                    .withAmount(oData.get("amount").getAsBigDecimal())
                    .withCurrency(oData.get("currency").getAsString()).withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing).withAddress(shippingAddress, AddressType.Shipping)
                    .withAddressMatchIndicator(false)
                    //.withCustomerEmail(requestData.get("payer").getAsJsonObject().get("email").getAsString())
                    .withAuthenticationSource(
                            enumHelper.getAuthenticationSourceByName(requestData.get("authenticationSource").getAsString()))
                    //.withAuthenticationRequestType(AuthenticationRequestType
                    //.valueOf(requestData.get("authenticationRequestType").getAsString()))
                    //.withMessageCategory(MessageCategory.valueOf(requestData.get("messageCategory").getAsString()))
                    //.withChallengeRequestIndicator(ChallengeRequestIndicator
                    //.valueOf(requestData.get("challengeRequestIndicator").getAsString()))
                    .withBrowserData(browserData).withMethodUrlCompletion(methodUrlCompletion).execute();

        } catch (ApiException e) {
            // TODO: add your error handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("liabilityShift", threeDSecureData.getLiabilityShift());

        // Frictionless flow
        if (!threeDSecureData.getStatus().equals("CHALLENGE_REQUIRED")) {
            response.put("result", threeDSecureData.getStatus()); // SUCCESS_AUTHENTICATED
            response.put("authenticationValue", threeDSecureData.getAuthenticationValue()); // AJkBCSNYJwAAAAPol4CUdAAAAAA=
            response.put("serverTransactionId", threeDSecureData.getServerTransactionId()); // AUT_00668cda-df2a-4826-a3d6-b78d7234ca01
            response.put("messageVersion", threeDSecureData.getMessageVersion()); // 2.1.0
            response.put("eci", threeDSecureData.getEci()); // 05
        }
        /*
         * TODO: pass the relevant outcome to the client-side Sample string for
         * frictionless flow: {"liabilityShift":"YES","result":"SUCCESS_AUTHENTICATED",
         * "authenticationValue":"AJkBBidJlAAAAAPol4CUdAAAAAA=","serverTransactionId":
         * "AUT_a345aa7c-40b5-4800-b32a-950aa6cb7f59","messageVersion":"2.1.0","eci":
         * "05"}
         *
         * Sample string for authentication failed:
         * {"liabilityShift":"NO","result":"NOT_AUTHENTICATED","authenticationValue":
         * null,"serverTransactionId":"AUT_e289ac18-d819-4130-ac24-891edbcd5be5",
         * "messageVersion":"2.1.0","eci":"07"}
         */

        // Challenge flow
        else {
            response.put("liabilityShift", threeDSecureData.getLiabilityShift()); // null
            response.put("status", threeDSecureData.getStatus()); // CHALLENGE_REQUIRED
            response.put("challengeMandated", threeDSecureData.isChallengeMandated()); // true

            Map<String, String> challenge = new HashMap<>();
            challenge.put("requestUrl", threeDSecureData.getIssuerAcsUrl());
            challenge.put("encodedChallengeRequest", threeDSecureData.getPayerAuthenticationRequest()); // Very long base64 encoded string
            challenge.put("messageType", threeDSecureData.getMessageType()); // creq
            response.put("challenge", challenge);
        }

        /*
         * TODO: pass the relevant outcome to the client-side
         *
         * Sample string for challenge flow:
         * {"liabilityShift":null,"status":"CHALLENGE_REQUIRED","challengeMandated":true
         * ,"challenge":{"requestUrl":
         * "https:\/\/acs2p.test.gpe.cz\/tds\/challenge\/brw\/288acb20-b40a-41ec-8bc7-ed721434c48a"
         * ,"encodedChallengeRequest":
         * "ewogICJtZXNzYWdlVHlwZSIgOiAiQ1JlcSIsCiAgIm1lc3NhZ2VWZXJzaW9uIiA6ICIyLjEuMCIsCiAgInRocmVlRFNTZXJ2ZXJUcmFuc0lEIiA6ICIzYmI3NjcwZi1hOGVmLTRkOTAtYWVlMS03ZWRiYmFiOWVkNzAiLAogICJhY3NUcmFuc0lEIiA6ICIyODhhY2IyMC1iNDBhLTQxZWMtOGJjNy1lZDcyMTQzNGM0OGEiLAogICJjaGFsbGVuZ2VXaW5kb3dTaXplIiA6ICIwNCIKfQ"
         * ,"messageType":"creq"}}
         *
         */

        return response;
    }

}