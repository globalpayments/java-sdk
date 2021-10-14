package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public abstract class Credit implements IPaymentMethod, IEncryptable, ITokenizable, IChargable, IAuthable, IRefundable, IReversable, IVerifiable, IPrePayable, IBalanceable, ISecure3d {
    private EncryptionData encryptionData;
    private MobilePaymentMethodType mobileType;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Credit;
    protected ThreeDSecure threeDSecure;
    private String token;
    protected String cardType = "Unknown";
    protected boolean fleetCard;
    private String bankName;

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
    public String getBankName() {
        return bankName;
    }
    public void setBankName(String value) {
        this.bankName = value;
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
    public AuthorizationBuilder charge(double amount) {
        return charge(new BigDecimal(amount));
    }
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

    public AuthorizationBuilder loadReversal() {
        return loadReversal(null);
    }
    public AuthorizationBuilder loadReversal(BigDecimal amount) {
        return new AuthorizationBuilder(TransactionType.LoadReversal, this).withAmount(amount);
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

    public String tokenize() throws ApiException {
        return tokenize(true, "default");
    }

    @Override
    public String tokenize(String configName) throws ApiException {
        return tokenize(true, configName);
    }

    @Override
    public String tokenize(boolean verifyCard, String configName) throws ApiException {
        return tokenize(verifyCard, configName, PaymentMethodUsageMode.Multiple);
    }

    public String tokenize(boolean verifyCard, String configName, PaymentMethodUsageMode paymentMethodUsageMode) throws ApiException {
        if(configName == null) {
            configName = "default";
        }

        TransactionType type = verifyCard ? TransactionType.Verify : TransactionType.Tokenize;

        Transaction response =
                new AuthorizationBuilder(type, this)
                        .withRequestMultiUseToken(verifyCard)
                        .withPaymentMethodUsageMode(paymentMethodUsageMode)
                        .execute(configName);

        return response.getToken();
    }

    public boolean updateTokenExpiry() throws ApiException {
        return updateTokenExpiry("default");
    }

    /**
     * Updates the token expiry date with the values proced to the card object
     * @return a boolean value indicating success/failure
     */
    @Override
    public boolean updateTokenExpiry(String configName) throws ApiException {
        if(configName == null) {
            configName = "default";
        }

        if (StringUtils.isNullOrEmpty(token)) {
            throw new BuilderException("Token cannot be null");
        }

        new ManagementBuilder(TransactionType.TokenUpdate)
                        .withPaymentMethod(this)
                        .execute(configName);

        return true;
    }

    /**
     * Deletes the token associated with the current card object
     * @return a boolean value indicating success/failure
     */
    @Override
    public boolean deleteToken(String configName) throws ApiException {
        if(configName == null) {
            configName = "default";
        }

        if (StringUtils.isNullOrEmpty(token)) {
            throw new BuilderException("Token cannot be null");
        }

        new ManagementBuilder(TransactionType.TokenDelete)
                .withPaymentMethod(this)
                .execute(configName);

        return true;
    }

}