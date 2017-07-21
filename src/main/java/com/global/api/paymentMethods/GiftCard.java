package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.AliasAction;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.Transaction;

import java.math.BigDecimal;

public class GiftCard implements IPaymentMethod, IPrePayable, IBalanceable, IReversable, IChargable {
    private String alias;
    private String number;
    private String pin;
    private String token;
    private String trackData;
    private String value;
    private String valueType;

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
        this.value = alias;
        this.valueType = "Alias";
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
        this.value = number;
        this.valueType = "CardNbr";
    }

    public PaymentMethodType getPaymentMethodType() {
        return PaymentMethodType.Gift;
    }
    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
        this.value = token;
        this.valueType = "TokenValue";
    }

    public String getTrackData() {
        return trackData;
    }
    public void setTrackData(String trackData) {
        this.trackData = trackData;
        this.value = trackData;
        this.valueType = "TrackData";
    }

    public String getValue() { return this.value; }
    public String getValueType() { return this.valueType; }

    public AuthorizationBuilder addAlias(String phoneNumber) {
        return new AuthorizationBuilder(TransactionType.Alias, this).withAlias(AliasAction.Add, phoneNumber);
    }

    public AuthorizationBuilder activate() {
        return activate(null);
    }
    public AuthorizationBuilder activate(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Activate, this).withAmount(amount);
    }

    public AuthorizationBuilder addValue() { return addValue(null); }
    public AuthorizationBuilder addValue(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.AddValue, this).withAmount(amount);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(null);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType inquiry) {
        return new AuthorizationBuilder(TransactionType.Balance, this).withBalanceInquiryType(inquiry);
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this).withAmount(amount);
    }

    public AuthorizationBuilder deactivate() {
        return new AuthorizationBuilder(TransactionType.Deactivate, this);
    }

    public AuthorizationBuilder removeAlias(String phoneNumber) {
        return new AuthorizationBuilder(TransactionType.Alias, this).withAlias(AliasAction.Delete, phoneNumber);
    }

    public AuthorizationBuilder replaceWith(GiftCard newCard) {
        return new AuthorizationBuilder(TransactionType.Replace, this).withReplacementCard(newCard);
    }

    public AuthorizationBuilder reverse() { return reverse(null); }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reversal, this).withAmount(amount);
    }

    public AuthorizationBuilder rewards() {
        return rewards(null);
    }
    public AuthorizationBuilder rewards(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reward, this).withAmount(amount);
    }

    public static GiftCard create(String phoneNumber) throws ApiException {
        GiftCard card = new GiftCard();

        Transaction response = new AuthorizationBuilder(TransactionType.Alias, card)
                .withAlias(AliasAction.Create, phoneNumber)
                .execute();

        if(response.getResponseCode().equals("00"))
            return response.getGiftCard();
        throw new GatewayException("Failed to create gift card.", response.getResponseCode(), response.getResponseMessage());
    }
}
