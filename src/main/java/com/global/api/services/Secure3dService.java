package com.global.api.services;

import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.TransactionType;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.ISecure3d;

public class Secure3dService {
    public static Secure3dBuilder checkEnrollment(IPaymentMethod paymentMethod) {
        return new Secure3dBuilder(TransactionType.VerifyEnrolled)
                .withPaymentMethod(paymentMethod);
    }

    public static Secure3dBuilder initiateAuthentication(IPaymentMethod paymentMethod, ThreeDSecure secureEcom) {
        if(paymentMethod instanceof ISecure3d) {
            ((ISecure3d) paymentMethod).setThreeDSecure(secureEcom);
        }

        return new Secure3dBuilder(TransactionType.InitiateAuthentication)
                .withPaymentMethod(paymentMethod);
    }

    public static Secure3dBuilder getAuthenticationData() {
        return new Secure3dBuilder(TransactionType.VerifySignature);
    }
}
