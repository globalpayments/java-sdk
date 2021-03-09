package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.utils.CardUtils;
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

    @Override
    public String tokenize() {
        return tokenize(true, "default");
    }

    @Override
    public String tokenize(boolean validatecard) {
    	return tokenize(validatecard, "default");
    }

    @Override
    public String tokenize(String configName) {
    	return tokenize(true, configName);
    }

    @Override
    public String tokenize(boolean validatecard, String configName) {
        return tokenizeWithIdempotencyKey(validatecard, configName,  null);
    }

    public String tokenizeWithIdempotencyKey(boolean validatecard, String configName, String idempotencyKey) {
        try {
            AuthorizationBuilder ab = new AuthorizationBuilder(validatecard ? TransactionType.Verify : TransactionType.Tokenize, this);

            if(idempotencyKey != null) {
                ab.withIdempotencyKey(idempotencyKey);
            }

            Transaction response =
                    ab
                            .withRequestMultiUseToken(true)
                            .execute(configName);

            return response.getToken();
        }
        catch(ApiException e) { return null; }
    }

    public boolean updateTokenExpiry() {
        try {
            return updateTokenExpiry("default");
        } catch (BuilderException e) {
            return false;
        }
    }

    /// <summary>
    /// Updates the token expiry date with the values proced to the card object
    /// </summary>
    /// <returns>boolean value indcating success/failure</returns>
    @Override
    public boolean updateTokenExpiry(String configName) throws BuilderException {
        return updateTokenExpiryWithIdemPotencyKey(configName, null);
    }

    public boolean updateTokenExpiryWithIdemPotencyKey(String configName, String idemPotencyKey) throws BuilderException {
        if (StringUtils.isNullOrEmpty(token)) {
            throw new BuilderException("Token cannot be null");
        }

        try {
            ManagementBuilder mb = new ManagementBuilder(TransactionType.TokenUpdate);

            if(idemPotencyKey != null) {
                mb.withIdempotencyKey(idemPotencyKey);
            }

            mb
                .withPaymentMethod(this)
                .execute(configName);

            return true;
        }
        catch (ApiException e) {
            return false;
        }
    }

    public boolean deleteToken() {
        try {
            return deleteToken("default");
        } catch (BuilderException e) {
            return false;
        }
    }

    /// <summary>
    /// Deletes the token associated with the current card object
    /// </summary>
    /// <returns>boolean value indicating success/failure</returns>
    @Override
    public boolean deleteToken(String configName) throws BuilderException {
        return deleteTokenWithIdempotencyKey(configName, null);
    }

    public boolean deleteTokenWithIdempotencyKey(String configName, String idempotencyKey) throws BuilderException {
        if (StringUtils.isNullOrEmpty(token)) {
            throw new BuilderException("Token cannot be null");
        }

        try {
            ManagementBuilder mb = new ManagementBuilder(TransactionType.TokenDelete);

            if(idempotencyKey != null) {
                mb.withIdempotencyKey(idempotencyKey);
            }

            mb
                .withPaymentMethod(this)
                .execute(configName);

            return true;
        }
        catch (ApiException e) {
            return false;
        }
    }

}
