/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.challengenotificationurl;

import com.google.gson.Gson;

import java.util.Base64;
import java.util.Hashtable;

class ChallengeNotificationHandler {


    Hashtable<String, String> handleChallengeNotification(String cres) {

        System.out.println();
        System.out.println("*** 05 *** ChallengeNotificationHandler");
        System.out.println();

        // example CRes (Challenge Result) sent by the ACS
        /*
         * String cres =
         * "eyJ0aHJlZURTU2VydmVyVHJhbnNJRCI6ImFmNjVjMzY5LTU5YjktNGY4ZC1iMmY2LTdkN2Q1ZjVjNjlkNSIsImF"
         * +
         * "jc1RyYW5zSUQiOiIxM2M3MDFhMy01YTg4LTRjNDUtODllOS1lZjY1ZTUwYThiZjkiLCJjaGFsbGVuZ2VDb21wbGV0a"
         * +
         * "W9uSW5kIjoiWSIsIm1lc3NhZ2VUeXBlIjoiQ3JlcyIsIm1lc3NhZ2VWZXJzaW9uIjoiMi4xLjAiLCJ0cmFuc"
         * + "1N0YXR1cyI6IlkifQ==";
         */

        try {

            byte[] decodedBytes = Base64.getDecoder().decode(cres);
            String challengeUrlResponseString = new String(decodedBytes);
            Gson gson = new Gson();

            // map to a custom class ChallengeUrlResponse which has String variables for
            // each response element
            ChallengeUrlResponse challengeUrlResponse = gson.fromJson(challengeUrlResponseString,
                    ChallengeUrlResponse.class);

            String threeDSServerTransID = challengeUrlResponse.getThreeDSServerTransID(); // af65c369-59b9-4f8d-b2f6-7d7d5f5c69d5
            String acsTransId = challengeUrlResponse.getAcsTransId(); // 13c701a3-5a88-4c45-89e9-ef65e50a8bf9
            String messageType = challengeUrlResponse.getMessageType(); // Cres
            String messageVersion = challengeUrlResponse.getMessageVersion(); // 2.1.0
            String transStatus = challengeUrlResponse.getTransStatus(); // Y

            if (!transStatus.equals("Y")) {
                // TODO check if this flow is okay.
                // TODO decline, return error message
            }

            // TODO: simple example of how to prepare the JSON string for JavaScript Library
            // (optional)
            Hashtable<String, String> responseObject = new Hashtable<String, String>();
            responseObject.put("threeDSServerTransID", threeDSServerTransID);
            responseObject.put("transStatus", transStatus);

            // TODO: notify client-side that the Challenge step is complete

            return responseObject;
        } catch (Exception e) {
            // TODO: Add your exception handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }
    }

}
