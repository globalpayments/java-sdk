package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.MerchantDataCollection;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.CardUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class CreditCardData extends Credit implements ICardData {
    @Getter @Setter private String cardHolderName;
    @Getter @Setter private boolean cardPresent = false;
    private String cvn;
    @Getter @Setter private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    @Getter @Setter private String eci;
    @Getter @Setter private ManualEntryMethod entryMethod;
    @Getter @Setter private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent = false;

    public String getCvn() {
        return cvn;
    }
    public void setCvn(String cvn) {
        if(cvn != null && !cvn.equals("")) {
            this.cvn = cvn;
            this.cvnPresenceIndicator = CvnPresenceIndicator.Present;
        }
    }
    public Integer getExpYear() {
        return expYear;
    }
    // NOTE: In .NET setExpYear() has a custom logic
    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
        try {
            this.cardType = CardUtils.mapCardType(number);
            this.fleetCard = CardUtils.isFleet(cardType, number);
        } catch (Exception e) {
            cardType = "Unknown";
        }
    }
    public String getShortExpiry() {
        if(expMonth != null && expYear != null) {
            return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
        }
        return null;
    }
    public boolean isReaderPresent() {
        return readerPresent;
    }
    public void setReaderPresent(boolean readerPresent) {
        this.readerPresent = readerPresent;
    }

    public CreditCardData() {
    }
    public CreditCardData(String token) {
        this();
        this.setToken(token);
    }

    public AuthorizationBuilder getDccRate() {
        return getDccRate(null, null);
    }

    public AuthorizationBuilder getDccRate(DccRateType dccRateType, DccProcessor dccProcessor) {
        DccRateData dccRateData = new DccRateData();
        dccRateData.setDccRateType(dccRateType == null ? DccRateType.None : dccRateType);
        dccRateData.setDccProcessor(dccProcessor == null ? DccProcessor.None : dccProcessor);

        return new AuthorizationBuilder(TransactionType.DccRateLookup, this)
                .withDccRateData(dccRateData);
    }

    public boolean hasInAppPaymentData() {
        return !StringUtils.isNullOrEmpty(this.getToken()) && this.getMobileType() != null;
    }

    /**
     * @deprecated verifyEnrolled is deprecated. Please use CheckEnrollment from Secure3dService
     */
    @Deprecated
    public boolean verifyEnrolled(BigDecimal amount, String currency) throws ApiException {
        return verifyEnrolled(amount, currency, null, "default");
    }
    /**
     * @deprecated verifyEnrolled is deprecated. Please use CheckEnrollment from Secure3dService
     */
    @Deprecated
    public boolean verifyEnrolled(BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifyEnrolled(amount, currency, orderId, "default");
    }
    /**
     * @deprecated verifyEnrolled is deprecated. Please use CheckEnrollment from Secure3dService
     */
    @Deprecated
    public boolean verifyEnrolled(BigDecimal amount, String currency, String orderId, String configName) throws ApiException {
        Transaction response;
        try{
            response = new AuthorizationBuilder(TransactionType.VerifyEnrolled, this)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderId(orderId)
                    .execute(configName);
        }
        catch (GatewayException exc) {
            if(exc.getResponseCode().equals("110")) {
                return false;
            }
            throw exc;
        }

        ThreeDSecure secureEcom = response.getThreeDsecure();
        if(secureEcom != null && secureEcom.isEnrolled()) {
            this.threeDSecure = secureEcom;
            this.threeDSecure.setAmount(amount);
            this.threeDSecure.setCurrency(currency);
            this.threeDSecure.setOrderId(response.getOrderId());
            return true;
        }

        return false;
    }

    /**
     * @deprecated verifySignature is deprecated. Please use getAuthenticationData from Secure3dService
     */
    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifySignature(authorizationResponse, amount, currency, orderId, "default");
    }
    /**
     * @deprecated verifySignature is deprecated. Please use getAuthenticationData from Secure3dService
     */
    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId, String configName) throws ApiException {
        // ensure we have an object
        if(this.threeDSecure == null)
            this.threeDSecure = new ThreeDSecure();

        this.threeDSecure.setAmount(amount);
        this.threeDSecure.setCurrency(currency);
        this.threeDSecure.setOrderId(orderId);

        return verifySignature(authorizationResponse, null, configName);
    }
    /**
     * @deprecated verifySignature is deprecated. Please use getAuthenticationData from Secure3dService
     */
    public boolean verifySignature(String authorizationResponse) throws ApiException {
        return verifySignature(authorizationResponse, null, "default");
    }
    /**
     * @deprecated verifySignature is deprecated. Please use getAuthenticationData from Secure3dService
     */
    public boolean verifySignature(String authorizationResponse, MerchantDataCollection merchantData) throws ApiException {
        return verifySignature(authorizationResponse, merchantData, "default");
    }
    /**
     * @deprecated verifySignature is deprecated. Please use getAuthenticationData from Secure3dService
     */
    public boolean verifySignature(String authorizationResponse, MerchantDataCollection merchantData, String configName) throws ApiException {
        // ensure we have an object
        if(threeDSecure == null) {
            threeDSecure = new ThreeDSecure();
        }

        // if we have merchant data use it
        if(merchantData != null) {
            this.threeDSecure.setMerchantData(merchantData);
        }

        TransactionReference ref = new TransactionReference();
        ref.setOrderId(threeDSecure.getOrderId());

        Transaction response = new ManagementBuilder(TransactionType.VerifySignature)
                .withAmount(threeDSecure.getAmount())
                .withCurrency(threeDSecure.getCurrency())
                .withPayerAuthenticationResponse(authorizationResponse)
                .withPaymentMethod(ref)
                .execute(configName);

        if(response.getResponseCode().equals("00")) {
            this.threeDSecure.setStatus(response.getThreeDsecure().getStatus());
            this.threeDSecure.setEci(response.getThreeDsecure().getEci());
            this.threeDSecure.setCavv(response.getThreeDsecure().getCavv());
            this.threeDSecure.setAlgorithm(response.getThreeDsecure().getAlgorithm());
            this.threeDSecure.setXid(response.getThreeDsecure().getXid());
            return true;
        }
        return false;
    }

}