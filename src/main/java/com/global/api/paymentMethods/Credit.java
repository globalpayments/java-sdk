package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.CardUtils;

import java.math.BigDecimal;

public abstract class Credit implements IPaymentMethod, IEncryptable, ITokenizable, IChargable, IAuthable, IRefundable, IReversable, IVerifiable, IPrePayable, IBalanceable, ISecure3d {
    private EncryptionData encryptionData;
    private MobilePaymentMethodType mobileType;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Credit;
    protected ThreeDSecure threeDSecure;
    private String token;
    protected String cardType = "Unknown";
    protected boolean fleetCard;

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
    public MobilePaymentMethodType getMobileType() {
		return mobileType;
	}
	public void setMobileType(MobilePaymentMethodType mobileType) {
		this.mobileType = mobileType;
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
    public boolean isFleet() {
        return fleetCard;
    }
    public void setFleetCard(boolean fleetCard) {
        this.fleetCard = fleetCard;
    }

    public AuthorizationBuilder authorize() { return authorize(null, false); }
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, false);
    }
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimated) {
        return new AuthorizationBuilder(TransactionType.Auth, this)
                .withAmount(amount != null ? amount : threeDSecure != null ? threeDSecure.getAmount() : null)
                .withCurrency(threeDSecure != null ? threeDSecure.getCurrency() : null)
                .withOrderId(threeDSecure != null ? threeDSecure.getOrderId() : null)
                .withAmountEstimated(isEstimated);
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
    	return tokenize(true, "default");
    }
    public String tokenize(boolean validatecard) {
    	return tokenize(validatecard, "default");
    }
    public String tokenize(String configName) { 
    	return tokenize(true, configName);
    }
    public String tokenize(boolean validatecard, String configName) {
        try {
            Transaction response = new AuthorizationBuilder(validatecard ? TransactionType.Verify : TransactionType.Tokenize, this)
            		.withRequestMultiUseToken(true)
            		.execute("tokenConfig");
            return response.getToken();
        }
        catch(ApiException e) { return null; }
    }
}
