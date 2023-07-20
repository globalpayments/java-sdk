/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.methodnotificationurl;

import com.google.gson.Gson;

import java.util.Base64;

class MethodNotificationUrlHandler {

    String handleMethodNotification(String threeDSMethodData) {

        System.out.println();
        System.out.println("*** 03 *** MethodNotificationUrlHandler");
        System.out.println();

        // sample ACS response for Method URL Response Notification
        // String threeDSMethodData =
        // "eyJ0aHJlZURTU2VydmVyVHJhbnNJRCI6ImFmNjVjMzY5LTU5YjktNGY4ZC1iMmY2LTdkN2Q1ZjVjNjlkNSJ9";

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(threeDSMethodData);
            String methodUrlResponseString = new String(decodedBytes);
            Gson gson = new Gson();

            // map to a custom class MethodUrlResponse
            MethodUrlResponse response = gson.fromJson(methodUrlResponseString, MethodUrlResponse.class);

            String threeDSServerTransID = response.getThreeDSServerTransID(); // // af65c369-59b9-4f8d-b2f6-7d7d5f5c69d5

            // TODO: notify client-side that the Method URL step is complete
            // optional to return decoded JSON string, see below
            return threeDSServerTransID;
        } catch (Exception e) {
            // TODO: add your exception handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }
    }

}