package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public abstract class Debit implements IPaymentMethod, IPrePayable, IRefundable, IReversable, IChargable, IEncryptable, IPinProtected, IAuthable {
    private EncryptionData encryptionData;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Debit;
    private String pinBlock;
    protected String cardType = "Unknown";
    @Getter @Setter
    private String tokenizedData;

    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public EncryptionData getEncryptionData() {
        return encryptionData;
    }
    public void setEncryptionData(EncryptionData encryptionData) {
        this.encryptionData = encryptionData;
    }
    public String getPinBlock() {
        return pinBlock;
    }
    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }
    public PaymentMethodType getPaymentMethodType() { return paymentMethodType; }

    public AuthorizationBuilder addValue() { return addValue(null); }
    public AuthorizationBuilder addValue(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.AddValue, this).withAmount(amount);
    }

    public AuthorizationBuilder authorize() { return authorize(null, true); }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, true);
    }
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimated) {
        return new AuthorizationBuilder(TransactionType.Auth, this)
                .withAmount(amount)
                .withAmountEstimated(true);
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this).withAmount(amount);
    }

    public AuthorizationBuilder refund() { return refund(null); }
    public AuthorizationBuilder refund(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Refund, this).withAmount(amount);
    }

    public AuthorizationBuilder reverse() { return reverse(null); }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reversal, this).withAmount(amount);
    }

    public AuthorizationBuilder fileAction() {
        return new AuthorizationBuilder(TransactionType.FileAction, this);
    }

    public AuthorizationBuilder balanceInquiry() {
        return balanceInquiry(null);
    }
    public AuthorizationBuilder balanceInquiry(InquiryType inquiry) {
        return new AuthorizationBuilder(TransactionType.Balance, this).withBalanceInquiryType(inquiry);
    }

}
