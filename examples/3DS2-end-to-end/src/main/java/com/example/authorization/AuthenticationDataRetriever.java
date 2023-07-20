/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.authorization;

import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.Secure3dService;

class AuthenticationDataRetriever {

    ThreeDSecure getAuthenticationData(String serverTransactionId, String paymentToken) {

        System.out.println();
        System.out.println("*** 06 *** AuthenticationDataRetriever");
        System.out.println();

        CreditCardData paymentMethod = new CreditCardData();
        paymentMethod.setToken(paymentToken);

        try {
            ThreeDSecure threeDSecure = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(serverTransactionId).execute();

            return threeDSecure;

        } catch (ApiException e) {
            // TODO: add your error handling here
            e.printStackTrace();
            throw new RuntimeException("just fail fast in this example");
        }

    }

}