package com.global.api.services;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;

public class CheckService {
    public CheckService(ServicesConfig config) throws ApiException {
        ServicesContainer.configure(config);
    }

    // Recurring

    // Charge
    public AuthorizationBuilder charge() {
        return charge(null);
    }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale).withAmount(amount);
    }

    // Void
    public ManagementBuilder voidTransaction(String transactionId) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.ACH);
        reference.setTransactionId(transactionId);

        return new ManagementBuilder(TransactionType.Void).withPaymentMethod(reference);
    }
}
