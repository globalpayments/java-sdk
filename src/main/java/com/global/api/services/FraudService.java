package com.global.api.services;

import com.global.api.builders.FraudBuilder;
import com.global.api.entities.RiskAssessment;
import com.global.api.entities.enums.TransactionType;
import com.global.api.paymentMethods.IPaymentMethod;

public class FraudService {
    public static FraudBuilder<RiskAssessment> RiskAssess(IPaymentMethod paymentMethod) {
        return
                new FraudBuilder<RiskAssessment>(TransactionType.RiskAssess)
                        .WithPaymentMethod(paymentMethod);
    }
}
