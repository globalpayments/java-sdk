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

import java.math.BigDecimal;

public class CreditCardData extends Credit implements ICardData {
    private String cardHolderName;
    private boolean cardPresent = false;
    private String cvn;
    private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent = false;

    public String getCardHolderName() {
        return cardHolderName;
    }
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    public boolean isCardPresent() {
        return cardPresent;
    }
    public void setCardPresent(boolean cardPresent) {
        this.cardPresent = cardPresent;
    }
    public String getCvn() {
        return cvn;
    }
    public void setCvn(String cvn) {
        if(cvn != null && !cvn.equals("")) {
            this.cvn = cvn;
            this.cvnPresenceIndicator = CvnPresenceIndicator.Present;
        }
    }
    public CvnPresenceIndicator getCvnPresenceIndicator() {
        return cvnPresenceIndicator;
    }
    public void setCvnPresenceIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        this.cvnPresenceIndicator = cvnPresenceIndicator;
    }
    public Integer getExpMonth() {
        return expMonth;
    }
    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }
    public Integer getExpYear() {
        return expYear;
    }
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

    public AuthorizationBuilder getDccRate(DccRateType dccRateType, DccProcessor dccProcessor) {
        DccRateData dccRateData = new DccRateData();
        dccRateData.setDccRateType(dccRateType);
        dccRateData.setDccProcessor(dccProcessor);

        return new AuthorizationBuilder(TransactionType.DccRateLookup, this)
                .withDccRateData(dccRateData);
    }

    public boolean verifyEnrolled(BigDecimal amount, String currency) throws ApiException {
        return verifyEnrolled(amount, currency, null, "default");
    }
    public boolean verifyEnrolled(BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifyEnrolled(amount, currency, orderId, "default");
    }
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

    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId) throws ApiException {
        return verifySignature(authorizationResponse, amount, currency, orderId, "default");
    }
    public boolean verifySignature(String authorizationResponse, BigDecimal amount, String currency, String orderId, String configName) throws ApiException {
        // ensure we have an object
        if(this.threeDSecure == null)
            this.threeDSecure = new ThreeDSecure();

        this.threeDSecure.setAmount(amount);
        this.threeDSecure.setCurrency(currency);
        this.threeDSecure.setOrderId(orderId);

        return verifySignature(authorizationResponse, null, configName);
    }
    public boolean verifySignature(String authorizationResponse) throws ApiException {
        return verifySignature(authorizationResponse, null, "default");
    }
    public boolean verifySignature(String authorizationResponse, MerchantDataCollection merchantData) throws ApiException {
        return verifySignature(authorizationResponse, merchantData, "default");
    }
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
