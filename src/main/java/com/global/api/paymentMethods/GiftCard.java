package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.Transaction;
import com.global.api.utils.CardUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GiftCard implements IPaymentMethod, IPrePayable, IBalanceable, IReversable, IChargable, IAuthable, IRefundable, IEncryptable {
    private String alias;
    private String cardType;
    private String expiry;
    private String number;
    private String pan;
    private String pin;
    private String token;
    private String trackData;
    private TrackNumber trackNumber;
    private String value;
    private String valueType;
    @Getter
    @Setter
    private EncryptionData encryptionData;
    @Getter
    @Setter
    private String encryptedPan;
    private EntryMethod entryMethod = EntryMethod.Swipe;
    public EntryMethod getEntryMethod() {
        return entryMethod;
    }
    public void setEntryMethod(EntryMethod entryMethod) {

        this.entryMethod = entryMethod;
    }


    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
        this.value = alias;
        this.valueType = "Alias";
    }

    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getExpiry() {
        return expiry;
    }
    public void setExpiry(String value) {
        expiry = value;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        if(StringUtils.isNullOrEmpty(value)) {
            setValue(number);
        }
        else {
            this.number = number;
            this.valueType = "CardNbr";
        }
    }

    public String getPan() {
        return pan;
    }
    public void setPan(String value) {
        pan = value;
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
        if(StringUtils.isNullOrEmpty(value)) {
            setValue(trackData);
        }
        else {
            this.trackData = trackData;
            this.valueType = "TrackData";
        }
    }

    public TrackNumber getTrackNumber() {
        return trackNumber;
    }
    public void setTrackNumber(TrackNumber trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;

        CardUtils.parseTrackData(this);
        if(StringUtils.isNullOrEmpty(trackData)) {
            setNumber(value);
            setPan(value);
        }
        cardType = CardUtils.mapCardType(pan);
    }

    public String getValueType() { return this.valueType; }

    @Getter
    @Setter
    private String tokenizationData;

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

    public AuthorizationBuilder authorize() {
        return authorize(null, false);
    }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, false);
    }
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimate) {
        return new AuthorizationBuilder(TransactionType.Auth, this)
                .withAmount(amount)
                .withAmountEstimated(isEstimate);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(null);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType inquiry) {
        return new AuthorizationBuilder(TransactionType.Balance, this).withBalanceInquiryType(inquiry);
    }

    public AuthorizationBuilder cashOut() {
        return new AuthorizationBuilder(TransactionType.CashOut, this);
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this).withAmount(amount);
    }
    public AuthorizationBuilder capture() { return capture(null); }
    public AuthorizationBuilder capture(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Capture, this).withAmount(amount);
    }


    public AuthorizationBuilder deactivate() {
        return new AuthorizationBuilder(TransactionType.Deactivate, this);
    }

    public AuthorizationBuilder refund() {
        return refund(null);
    }
    public AuthorizationBuilder refund(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Refund, this)
                .withAmount(amount);
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

    public AuthorizationBuilder issue(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Issue, this).withAmount(amount);
    }

    public AuthorizationBuilder fileAction() {
        return new AuthorizationBuilder(TransactionType.FileAction, this);
    }

}
