package com.global.api.entities;

import com.global.api.entities.enums.ExemptStatus;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.ReverseStringEnumMap;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ThreeDSecure {
    private String acsTransactionId;
    private String acsEndVersion;
    private String acsStartVersion;
    private ArrayList<String> acsInfoIndicator;
    private int algorithm;
    private BigDecimal amount;
    private String authenticationSource;
    private String authenticationType;
    private String authenticationValue;
    private String cardHolderResponseInfo;
    private String cavv;
    private boolean challengeMandated;
    private String criticalityIndicator;
    private String currency;
    private String decoupledResponseIndicator;
    private String directoryServerTransactionId;
    private String directoryServerEndVersion;
    private String directoryServerStartVersion;
    private String eci;
    private boolean enrolled;
    private ExemptStatus exemptStatus;
    private String exemptReason;
    private String issuerAcsUrl;
    public String challengeReturnUrl;
    public String sessionDataFieldName;
    public String messageType;
    private MerchantDataCollection merchantData;
    private String messageCategory;
    private String messageExtensionData;
    private String messageExtensionId;
    private String messageExtensionName;
    private String messageVersion;
    private String orderId;
    private String payerAuthenticationRequest;
    private String paymentDataSource;
    private String paymentDataType;
    private String sdkInterface;
    private String sdkUiType;
    private String serverTransactionId;
    private String status;
    private String statusReason;
    private Secure3dVersion version;
    private String whitelistStatus;
    private String xid;

    public String getAcsTransactionId() {
        return acsTransactionId;
    }
    public void setAcsTransactionId(String acsTransactionId) {
        this.acsTransactionId = acsTransactionId;
    }
    public String getAcsEndVersion() {
        return acsEndVersion;
    }
    public void setAcsEndVersion(String acsEndVersion) {
        this.acsEndVersion = acsEndVersion;
    }
    public String getAcsStartVersion() {
        return acsStartVersion;
    }
    public void setAcsStartVersion(String acsStartVersion) {
        this.acsStartVersion = acsStartVersion;
    }
    public ArrayList<String> getAcsInfoIndicator() {
        return acsInfoIndicator;
    }
    public void setAcsInfoIndicator(ArrayList<String> acsInfoIndicator) {
        this.acsInfoIndicator = acsInfoIndicator;
    }
    public int getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) throws ApiException {
        this.amount = amount;
        getMerchantData().put("_amount", amount != null ? amount.toString() : null, false);
    }
    public String getAuthenticationSource() {
        return authenticationSource;
    }
    public void setAuthenticationSource(String authenticationSource) {
        this.authenticationSource = authenticationSource;
    }
    public String getAuthenticationType() {
        return authenticationType;
    }
    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
    public String getAuthenticationValue() {
        return authenticationValue;
    }
    public void setAuthenticationValue(String authenticationValue) {
        this.authenticationValue = authenticationValue;
    }
    public String getCardHolderResponseInfo() {
        return cardHolderResponseInfo;
    }
    public void setCardHolderResponseInfo(String cardHolderResponseInfo) {
        this.cardHolderResponseInfo = cardHolderResponseInfo;
    }
    public String getCavv() {
        return cavv;
    }
    public void setCavv(String cavv) {
        this.cavv = cavv;
    }
    public boolean isChallengeMandated() {
        return challengeMandated;
    }
    public void setChallengeMandated(boolean challengeMandated) {
        this.challengeMandated = challengeMandated;
    }
    public String getCriticalityIndicator() {
        return criticalityIndicator;
    }
    public void setCriticalityIndicator(String criticalityIndicator) {
        this.criticalityIndicator = criticalityIndicator;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) throws ApiException {
        this.currency = currency;
        getMerchantData().put("_currency", currency, false);
    }
    public String getDecoupledResponseIndicator() {
        return decoupledResponseIndicator;
    }
    public void setDecoupledResponseIndicator(String decoupledResponseIndicator) {
        this.decoupledResponseIndicator = decoupledResponseIndicator;
    }
    public String getDirectoryServerTransactionId() {
        return directoryServerTransactionId;
    }
    public void setDirectoryServerTransactionId(String directoryServerTransactionId) {
        this.directoryServerTransactionId = directoryServerTransactionId;
    }
    public String getDirectoryServerEndVersion() {
        return directoryServerEndVersion;
    }
    public void setDirectoryServerEndVersion(String directoryServerEndVersion) {
        this.directoryServerEndVersion = directoryServerEndVersion;
    }
    public String getDirectoryServerStartVersion() {
        return directoryServerStartVersion;
    }
    public void setDirectoryServerStartVersion(String directoryServerStartVersion) {
        this.directoryServerStartVersion = directoryServerStartVersion;
    }
    public String getEci() {
        return eci;
    }
    public void setEci(String eci) {
        this.eci = eci;
    }
    public boolean isEnrolled() {
        return enrolled;
    }
    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }
    public ExemptStatus getExemptStatus() {
        return exemptStatus;
    }
    public void setExemptStatus(ExemptStatus exemptStatus) {
        this.exemptStatus = exemptStatus;
    }
    public String getExemptReason() {
        return exemptReason;
    }
    public void setExemptReason(String exemptReason) {
        this.exemptReason = exemptReason;
    }
    public String getIssuerAcsUrl() {
        return issuerAcsUrl;
    }
    public String getChallengeReturnUrl() {
        return challengeReturnUrl;
    }
    public void setChallengeReturnUrl(String challengeReturnUrl) {
        this.challengeReturnUrl = challengeReturnUrl;
    }
    public String getSessionDataFieldName() {
        return sessionDataFieldName;
    }
    public void setSessionDataFieldName(String sessionDataFieldName) {
        this.sessionDataFieldName = sessionDataFieldName;
    }
    public String getMessageType() {
        return messageType;
    }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    public void setIssuerAcsUrl(String issuerAcsUrl) {
        this.issuerAcsUrl = issuerAcsUrl;
    }
    public MerchantDataCollection getMerchantData() {
        if(merchantData == null)
            merchantData = new MerchantDataCollection();
        return merchantData;
    }
    public void setMerchantData(MerchantDataCollection merchantData) {
        if(this.merchantData != null)
            merchantData.mergeHidden(this.merchantData);

        this.merchantData = merchantData;
        if(merchantData.hasKey("_amount")) {
            this.amount = merchantData.getDecimal("_amount");
        }
        if(merchantData.hasKey("_currency")) {
            this.currency = merchantData.getString("_currency");
        }
        if(merchantData.hasKey("_orderId")) {
            this.orderId = merchantData.getString("_orderId");
        }
        if(merchantData.hasKey("_version")) {
            this.version = ReverseStringEnumMap.parse(merchantData.getString("_version"), Secure3dVersion.class);
        }
    }
    public String getMessageCategory() {
        return messageCategory;
    }
    public void setMessageCategory(String messageCategory) {
        this.messageCategory = messageCategory;
    }
    public String getMessageExtensionData() {
        return messageExtensionData;
    }
    public void setMessageExtensionData(String messageExtensionData) {
        this.messageExtensionData = messageExtensionData;
    }
    public String getMessageExtensionId() {
        return messageExtensionId;
    }
    public void setMessageExtensionId(String messageExtensionId) {
        this.messageExtensionId = messageExtensionId;
    }
    public String getMessageExtensionName() {
        return messageExtensionName;
    }
    public void setMessageExtensionName(String messageExtensionName) {
        this.messageExtensionName = messageExtensionName;
    }
    public String getMessageVersion() {
        return messageVersion;
    }
    public void setMessageVersion(String messageVersion) {
        this.messageVersion = messageVersion;
    }
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) throws ApiException {
        this.orderId = orderId;
        getMerchantData().put("_orderId", orderId, false);
    }
    public String getPayerAuthenticationRequest() {
        return payerAuthenticationRequest;
    }
    public void setPayerAuthenticationRequest(String payerAuthenticationRequest) {
        this.payerAuthenticationRequest = payerAuthenticationRequest;
    }
    public String getPaymentDataSource() {
        return paymentDataSource;
    }
    public void setPaymentDataSource(String paymentDataSource) {
        this.paymentDataSource = paymentDataSource;
    }
    public String getPaymentDataType() {
        return paymentDataType;
    }
    public void setPaymentDataType(String paymentDataType) {
        this.paymentDataType = paymentDataType;
    }
    public String getSdkInterface() {
        return sdkInterface;
    }
    public void setSdkInterface(String sdkInterface) {
        this.sdkInterface = sdkInterface;
    }
    public String getSdkUiType() {
        return sdkUiType;
    }
    public void setSdkUiType(String sdkUiType) {
        this.sdkUiType = sdkUiType;
    }
    public String getServerTransactionId() {
        return serverTransactionId;
    }
    public void setServerTransactionId(String serverTransactionId) {
        this.serverTransactionId = serverTransactionId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusReason() {
        return statusReason;
    }
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
    public Secure3dVersion getVersion() {
        return version;
    }
    public void setVersion(Secure3dVersion version) throws ApiException {
        this.version = version;
        getMerchantData().put("_version", version.getValue(), false);
    }
    public String getWhitelistStatus() {
        return whitelistStatus;
    }
    public void setWhitelistStatus(String whitelistStatus) {
        this.whitelistStatus = whitelistStatus;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }

    public ThreeDSecure() {
        this.paymentDataType = "3DSecure";
    }

    public void merge(ThreeDSecure secureEcom) {
        if(secureEcom != null) {
            this.acsTransactionId = mergeValue(acsTransactionId, secureEcom.getAcsTransactionId());
            this.acsEndVersion = mergeValue(acsEndVersion, secureEcom.getAcsEndVersion());
            this.acsStartVersion = mergeValue(acsStartVersion, secureEcom.getAcsStartVersion());
            this.algorithm = mergeValue(algorithm, secureEcom.getAlgorithm());
            this.amount = mergeValue(amount, secureEcom.getAmount());
            this.authenticationSource = mergeValue(authenticationSource, secureEcom.getAuthenticationSource());
            this.authenticationType = mergeValue(authenticationType, secureEcom.getAuthenticationType());
            this.authenticationValue = mergeValue(authenticationValue, secureEcom.getAuthenticationValue());
            this.cardHolderResponseInfo = mergeValue(cardHolderResponseInfo, secureEcom.getCardHolderResponseInfo());
            this.cavv = mergeValue(cavv, secureEcom.getCavv());
            this.challengeMandated = mergeValue(challengeMandated, secureEcom.isChallengeMandated());
            this.criticalityIndicator = mergeValue(criticalityIndicator, secureEcom.getCriticalityIndicator());
            this.currency = mergeValue(currency, secureEcom.getCurrency());
            this.decoupledResponseIndicator = mergeValue(decoupledResponseIndicator, secureEcom.getDecoupledResponseIndicator());
            this.directoryServerTransactionId = mergeValue(directoryServerTransactionId, secureEcom.getDirectoryServerTransactionId());
            this.directoryServerEndVersion = mergeValue(directoryServerEndVersion, secureEcom.getDirectoryServerEndVersion());
            this.directoryServerStartVersion = mergeValue(directoryServerStartVersion, secureEcom.getDirectoryServerStartVersion());
            this.eci = mergeValue(eci, secureEcom.getEci());
            this.enrolled = mergeValue(enrolled, secureEcom.isEnrolled());
            this.issuerAcsUrl = mergeValue(issuerAcsUrl, secureEcom.getIssuerAcsUrl());
            this.messageCategory = mergeValue(messageCategory, secureEcom.getMessageCategory());
            this.messageExtensionId = mergeValue(messageExtensionId, secureEcom.getMessageExtensionId());
            this.messageExtensionName = mergeValue(messageExtensionName, secureEcom.getMessageExtensionName());
            this.messageVersion = mergeValue(messageVersion, secureEcom.getMessageVersion());
            this.orderId = mergeValue(orderId, secureEcom.getOrderId());
            this.payerAuthenticationRequest = mergeValue(payerAuthenticationRequest, secureEcom.getPayerAuthenticationRequest());
            this.paymentDataSource = mergeValue(paymentDataSource, secureEcom.getPaymentDataSource());
            this.paymentDataType = mergeValue(paymentDataType, secureEcom.getPaymentDataType());
            this.sdkInterface = mergeValue(sdkInterface, secureEcom.getSdkInterface());
            this.sdkUiType = mergeValue(sdkUiType, secureEcom.getSdkUiType());
            this.serverTransactionId = mergeValue(serverTransactionId, secureEcom.getServerTransactionId());
            this.status = mergeValue(status, secureEcom.getStatus());
            this.statusReason = mergeValue(statusReason, secureEcom.getStatusReason());
            this.version = mergeValue(version, secureEcom.getVersion());
            this.whitelistStatus = mergeValue(whitelistStatus, secureEcom.getWhitelistStatus());
            this.exemptStatus = mergeValue(exemptStatus, secureEcom.getExemptStatus());
            this.exemptReason = mergeValue(exemptReason, secureEcom.getExemptReason());
            this.xid = mergeValue(xid, secureEcom.getXid());

            //this.merchantData = mergeValue(merchantData, secureEcom.getMerchantData());
        }
    }

    private <T> T mergeValue(T currentValue, T mergeValue) {
        if(mergeValue == null) {
            return currentValue;
        }
        return mergeValue;
    }
}
