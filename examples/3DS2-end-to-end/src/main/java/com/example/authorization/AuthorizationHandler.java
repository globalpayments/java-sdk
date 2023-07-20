/*
 * this sample code is not specific to the Global Payments SDK and is intended as a simple example and
 * should not be treated as Production-ready code. You'll need to add your own message parsing and
 * security in line with your application or website
 */
package com.example.authorization;

import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;

import java.math.BigDecimal;

class AuthorizationHandler {

    AuthorizationResult authorize(ThreeDSecure threeDSecure, String paymentToken) {

        System.out.println();
        System.out.println("*** 07 *** AuthorizationHandler");
        System.out.println();

        boolean failedAuthentication = !threeDSecure.getLiabilityShift().equals("YES")
                || (!threeDSecure.getStatus().equals("SUCCESS_AUTHENTICATED")
                && !threeDSecure.getStatus().equals("SUCCESS_ATTEMPT_MADE"));

        Transaction transaction = null;
        if (!failedAuthentication) {
            CreditCardData paymentMethod = new CreditCardData();
            paymentMethod.setToken(paymentToken);
            paymentMethod.setThreeDSecure(threeDSecure);
            // proceed to authorization with liability shift
            try {
                transaction = paymentMethod.authorize(new BigDecimal(100)).withCurrency("EUR").execute();
            } catch (ApiException e) {
                // TODO: set your proper error handling here
                e.printStackTrace();
                throw new RuntimeException("just fail fast in this example");
            }
        }

        AuthorizationResult authorizationResult = new AuthorizationResult();
        authorizationResult.setFailedAuthentication(failedAuthentication);
        authorizationResult.setTransaction(transaction);

        return authorizationResult;
    }

}