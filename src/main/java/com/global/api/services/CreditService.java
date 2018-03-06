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

public class CreditService {
    public CreditService(ServicesConfig config) throws ApiException {
        ServicesContainer.configure(config);
    }

    public AuthorizationBuilder authorize() {
        return authorize(null);
    }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Auth).withAmount(amount);
    }

    public ManagementBuilder capture(String transactionId) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Credit);
        reference.setTransactionId(transactionId);

        return new ManagementBuilder(TransactionType.Capture)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder charge() {
        return charge(null);
    }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale).withAmount(amount);
    }

    public ManagementBuilder edit(String transactionId) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Credit);
        reference.setTransactionId(transactionId);

        return new ManagementBuilder(TransactionType.Edit)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder refund() {
        return refund(null);
    }
    public AuthorizationBuilder refund(BigDecimal amount) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Credit);

        return new AuthorizationBuilder(TransactionType.Refund)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder reverse() {
        return reverse(null);
    }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Credit);

        return new AuthorizationBuilder(TransactionType.Reversal)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder verify() {
        return new AuthorizationBuilder(TransactionType.Verify);
    }

    public ManagementBuilder voidTransaction() {
        return voidTransaction(null);
    }
    public ManagementBuilder voidTransaction(String transactionId) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Credit);
        reference.setTransactionId(transactionId);

        return new ManagementBuilder(TransactionType.Void)
                .withPaymentMethod(reference);
    }
}
