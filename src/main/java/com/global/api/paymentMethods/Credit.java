package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

import java.math.BigDecimal;

public abstract class Credit implements IPaymentMethod, IEncryptable, ITokenizable, IChargable, IAuthable, IRefundable, IReversable, IVerifiable, IPrePayable, IBalanceable, ISecure3d {
    private EncryptionData encryptionData;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Credit;
    protected ThreeDSecure threeDSecure;
    private String token;

    public EncryptionData getEncryptionData() {
        return encryptionData;
    }
    public void setEncryptionData(EncryptionData encryptionData) {
        this.encryptionData = encryptionData;
    }
    public PaymentMethodType getPaymentMethodType() { return paymentMethodType; }
    public ThreeDSecure getThreeDSecure() {
        return threeDSecure;
    }
    public void setThreeDSecure(ThreeDSecure threeDSecure) {
        this.threeDSecure = threeDSecure;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public AuthorizationBuilder authorize() { return authorize(null); }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Auth, this)
                .withAmount(amount != null ? amount : threeDSecure != null ? threeDSecure.getAmount() : null)
                .withCurrency(threeDSecure != null ? threeDSecure.getCurrency() : null)
                .withOrderId(threeDSecure != null ? threeDSecure.getOrderId() : null);
    }

    public AuthorizationBuilder charge() { return charge(null); }
    public AuthorizationBuilder charge(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Sale, this)
                .withAmount(amount != null ? amount : threeDSecure != null ? threeDSecure.getAmount() : null)
                .withCurrency(threeDSecure != null ? threeDSecure.getCurrency() : null)
                .withOrderId(threeDSecure != null ? threeDSecure.getOrderId() : null);
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

    public AuthorizationBuilder refund() { return refund(null); }
    public AuthorizationBuilder refund(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Refund, this).withAmount(amount);
    }

    public AuthorizationBuilder reverse() { return reverse(null); }
    public AuthorizationBuilder reverse(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.Reversal, this).withAmount(amount);
    }

    public AuthorizationBuilder verify() {
        return new AuthorizationBuilder(TransactionType.Verify, this);
    }

    public String tokenize() {
        try {
            Transaction response = new AuthorizationBuilder(TransactionType.Verify, this).withRequestMultiUseToken(true).execute();
            return response.getToken();
        }
        catch(ApiException e) { return null; }
    }
}
