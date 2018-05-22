package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.enums.AliasAction;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GatewayConfig;

import java.math.BigDecimal;

public class GiftService {
    public GiftService(GatewayConfig config) throws ApiException {
        ServicesContainer.configureService(config);
    }

    public AuthorizationBuilder activate() {
        return activate(null);
    }
    public AuthorizationBuilder activate(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Activate).withAmount(amount);
    }

    public AuthorizationBuilder addValue() {
        return addValue(null);
    }
    public AuthorizationBuilder addValue(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.AddValue).withAmount(amount);
    }

    public AuthorizationBuilder addAlias(String phoneNumber) {
        return new AuthorizationBuilder(TransactionType.Alias).withAlias(AliasAction.Add, phoneNumber);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(null);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType inquiry) {
        return new AuthorizationBuilder(TransactionType.Balance);
    }

    public AuthorizationBuilder charge() {
        return charge(null);
    }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale).withAmount(amount);
    }

    public GiftCard create(String phoneNumber) throws ApiException {
        return GiftCard.create(phoneNumber);
    }

    public AuthorizationBuilder deactivate() {
        return new AuthorizationBuilder(TransactionType.Deactivate);
    }

    public AuthorizationBuilder removeAlias(String phoneNumber) {
        return new AuthorizationBuilder(TransactionType.Alias).withAlias(AliasAction.Delete, phoneNumber);
    }

    public AuthorizationBuilder replaceWith(GiftCard newCard) {
        return new AuthorizationBuilder(TransactionType.Replace).withReplacementCard(newCard);
    }

    public AuthorizationBuilder reverse() {
        return reverse(null);
    }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        TransactionReference reference = new TransactionReference();
        reference.setPaymentMethodType(PaymentMethodType.Gift);

        return new AuthorizationBuilder(TransactionType.Reversal)
                .withAmount(amount)
                .withPaymentMethod(reference);
    }

    public AuthorizationBuilder rewards() {
        return rewards(null);
    }
    public AuthorizationBuilder rewards(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reward).withAmount(amount);
    }

    public ManagementBuilder voidTransaction(String transactionId) {
        TransactionReference reference = new TransactionReference();
        reference.setTransactionId(transactionId);
        reference.setPaymentMethodType(PaymentMethodType.Gift);

        return new ManagementBuilder(TransactionType.Void).withPaymentMethod(reference);
    }
}
