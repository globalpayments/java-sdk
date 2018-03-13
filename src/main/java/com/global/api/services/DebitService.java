package com.global.api.services;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;

public class DebitService {
    public DebitService(ServicesConfig config) throws ApiException {
        ServicesContainer.configure(config);
    }

    public AuthorizationBuilder charge() {
        return charge(null);
    }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale).withAmount(amount);
    }

    public AuthorizationBuilder refund() {
        return refund(null);
    }
    public AuthorizationBuilder refund(BigDecimal amount) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Debit);

        return new AuthorizationBuilder(TransactionType.Refund)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder reverse() {
        return reverse(null);
    }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Debit);

        return new AuthorizationBuilder(TransactionType.Reversal)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }
}
