package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GatewayConfig;

import java.math.BigDecimal;

public class EbtService {
    public EbtService(GatewayConfig config) throws ApiException {
        ServicesContainer.configureService(config);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(InquiryType.Foodstamp);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType type) {
        return new AuthorizationBuilder(TransactionType.Balance)
                .withBalanceInquiryType(type)
                .withAmount(new BigDecimal(0));
    }

    public AuthorizationBuilder benefitWithdrawal() {
        return benefitWithdrawal(null);
    }
    public AuthorizationBuilder benefitWithdrawal(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.BenefitWithdrawal)
                .withAmount(amount)
                .withCashBack(new BigDecimal(0));
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
        reference.setPaymentMethodType(PaymentMethodType.EBT);

        return new AuthorizationBuilder(TransactionType.Refund)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }
}
