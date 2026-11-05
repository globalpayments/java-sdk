package com.global.api.entities;

import com.global.api.entities.enums.ExemptStatus;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.enums.UCAFIndicator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.utils.ReverseStringEnumMap;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class ThreeDSecure {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private DateTime timeCreated;
    @Getter
    @Setter
    private String transactionType;
    @Getter
    @Setter
    private String channel;
    @Getter
    @Setter
    private String merchantId;
    @Getter
    @Setter
    private String merchantName;
    @Getter
    @Setter
    private String accountId;
    @Getter
    @Setter
    private String accountName;
    @Getter
    @Setter
    private String reference;
    @Getter
    @Setter
    private String country;
    public String challengeReturnUrl;
    public String sessionDataFieldName;
    public String messageType;
    public List<MessageExtension> messageExtensions;
    private String acsTransactionId;
    private String acsEndVersion;
    private String acsStartVersion;
    private ArrayList<String> acsInfoIndicator;
    @Getter
    @Setter
    private String acsInterface;
    @Getter
    @Setter
    private String acsUiTemplate;
    @Getter
    @Setter
    private String acsReferenceNumber;
    private int algorithm;
    private BigDecimal amount;
    private String authenticationSource;
    private String authenticationType;
    private String authenticationValue;
    private String cardHolderResponseInfo;
    private String cavv;
    private boolean challengeMandated;
    private String currency;
    private String decoupledResponseIndicator;
    private String directoryServerTransactionId;
    private String directoryServerEndVersion;
    private String directoryServerStartVersion;
    private String eci;
    private boolean enrolled;
    private String enrolledStatus;
    private ExemptStatus exemptStatus;
    private String exemptReason;
    private String issuerAcsUrl;
    private MerchantDataCollection merchantData;
    private String messageCategory;
    private String messageVersion;
    private String orderId;
    private String payerAuthenticationRequest;
    private PaymentDataSourceType paymentDataSource;
    private String paymentDataType;
    private String sdkInterface;
    private String sdkUiType;
    private String secureCode;
    private String serverTransactionId;
    private String status;
    private String statusReason;
    private Secure3dVersion version;
    private String whitelistStatus;
    private String xid;
    private String liabilityShift;
    private UCAFIndicator ucafIndicator;
    @Getter
    @Setter
    private String providerServerTransRef;
    @Getter @Setter
    private String preference;
    @Getter
    @Setter
    private String challengeWindowSize;
    @Getter
    @Setter
    private String deviceRenderingOptions;
    @Getter
    @Setter
    private Boolean frictionlessFlowEligible;
    @Getter
    @Setter
    private String exemptionRequestType;
    @Getter
    @Setter
    private String exemptionApprovalURL;
    @Getter
    @Setter
    private String exemptionRejectionReason;
    @Getter
    @Setter
    private String challengeType;
    @Getter
    @Setter
    private String creqData;
    @Getter
    @Setter
    private String acsSignedContent;
    @Getter
    @Setter
    private String challengeModel;
    @Getter
    @Setter
    private String redirectUrl;
    @Getter
    @Setter
    private String acsChallengeRequestUrl;
    @Getter
    @Setter
    private String methodUrl;
    @Getter
    @Setter
    private String threeDsServerTransId;
    @Getter
    @Setter
    private String threeDsMethodReturnUrl;
    @Getter
    @Setter
    private String encodedMethodData;
    @Getter
    @Setter
    private String decoupledFlowIndicator;
    @Getter
    @Setter
    private Integer decoupledFlowTimerValue;
    @Getter
    @Setter
    private String decoupledFlowTimerUnit;
    @Getter
    @Setter
    private String notificationURL;
    @Getter
    @Setter
    private Boolean hardcodedDecision;
    @Getter
    @Setter
    private String riskScore;
    @Getter
    @Setter
    private String riskAssessmentStatus;
    @Getter
    @Setter
    private String trustedMerchantExemption;
    @Getter
    @Setter
    private String lowValueExemption;
    @Getter
    @Setter
    private String transactionRiskAnalysisExemption;
    @Getter
    @Setter
    private String recurringExemption;
    @Getter
    @Setter
    private String aggregationData;
    @Getter
    @Setter
    private String acsRenderingType;
    @Getter
    @Setter
    private String methodUrlCompletionIndicator;
    @Getter
    @Setter
    private String threeRiRequestorPriorAuthenticationData;
    @Getter
    @Setter
    private String messageExtensionData;
    @Getter
    @Setter
    private String payerAuthenticationResponseDecrypted;
    @Getter
    @Setter
    private String authenticationRequestData;
    @Getter
    @Setter
    private String authenticationResponseData;
    @Getter
    @Setter
    private String actionId;
    @Getter
    @Setter
    private String actionType;
    @Getter
    @Setter
    private DateTime actionTimeCreated;
    @Getter
    @Setter
    private String actionResultCode;
    @Getter
    @Setter
    private String actionAppId;
    @Getter
    @Setter
    private String actionAppName;
    @Getter
    @Setter
    private String paymentMethodResult;
    @Getter
    @Setter
    private String paymentMethodMessage;
    @Getter
    @Setter
    private String paymentMethodEntryMode;
    @Getter
    @Setter
    private String paymentMethodName;
    @Getter
    @Setter
    private Card card;
    @Getter
    @Setter
    private DateTime timeLastUpdated;

    public ThreeDSecure() {
        this.paymentDataType = "3DSecure";
    }

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

    public String getEnrolledStatus() {
        return enrolledStatus;
    }

    public void setEnrolledStatus(String enrolledStatus) {
        this.enrolledStatus = enrolledStatus;
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

    public void setIssuerAcsUrl(String issuerAcsUrl) {
        this.issuerAcsUrl = issuerAcsUrl;
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

    public MerchantDataCollection getMerchantData() {
        if (merchantData == null)
            merchantData = new MerchantDataCollection();
        return merchantData;
    }

    public void setMerchantData(MerchantDataCollection merchantData) {
        if (this.merchantData != null)
            merchantData.mergeHidden(this.merchantData);

        this.merchantData = merchantData;
        if (merchantData.hasKey("_amount")) {
            this.amount = merchantData.getDecimal("_amount");
        }
        if (merchantData.hasKey("_currency")) {
            this.currency = merchantData.getString("_currency");
        }
        if (merchantData.hasKey("_orderId")) {
            this.orderId = merchantData.getString("_orderId");
        }
        if (merchantData.hasKey("_version")) {
            this.version = ReverseStringEnumMap.parse(merchantData.getString("_version"), Secure3dVersion.class);
        }
    }

    public String getMessageCategory() {
        return messageCategory;
    }

    public void setMessageCategory(String messageCategory) {
        this.messageCategory = messageCategory;
    }

    public List<MessageExtension> getMessageExtensions() {
        return messageExtensions;
    }

    public void setMessageExtensions(List<MessageExtension> messageExtensions) {
        this.messageExtensions = messageExtensions;
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

    public PaymentDataSourceType getPaymentDataSource() {
        return paymentDataSource;
    }

    public void setPaymentDataSource(PaymentDataSourceType paymentDataSource) {
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

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
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

    public String getLiabilityShift() {
        return liabilityShift;
    }

    public void setLiabilityShift(String liabilityShift) {
        this.liabilityShift = liabilityShift;
    }

    public UCAFIndicator getUcafIndicator() {
        return ucafIndicator;
    }

    public void setUcafIndicator(UCAFIndicator ucafIndicator) {
        this.ucafIndicator = ucafIndicator;
    }

    public void merge(ThreeDSecure secureEcom) {
        if (secureEcom != null) {
            this.id = mergeValue(id, secureEcom.getId());
            this.timeCreated = mergeValue(timeCreated, secureEcom.getTimeCreated());
            this.transactionType = mergeValue(transactionType, secureEcom.getTransactionType());
            this.channel = mergeValue(channel, secureEcom.getChannel());
            this.merchantId = mergeValue(merchantId, secureEcom.getMerchantId());
            this.merchantName = mergeValue(merchantName, secureEcom.getMerchantName());
            this.accountId = mergeValue(accountId, secureEcom.getAccountId());
            this.accountName = mergeValue(accountName, secureEcom.getAccountName());
            this.reference = mergeValue(reference, secureEcom.getReference());
            this.country = mergeValue(country, secureEcom.getCountry());
            this.acsTransactionId = mergeValue(acsTransactionId, secureEcom.getAcsTransactionId());
            this.acsEndVersion = mergeValue(acsEndVersion, secureEcom.getAcsEndVersion());
            this.acsStartVersion = mergeValue(acsStartVersion, secureEcom.getAcsStartVersion());
            this.acsInterface = mergeValue(acsInterface, secureEcom.getAcsInterface());
            this.acsUiTemplate = mergeValue(acsUiTemplate, secureEcom.getAcsUiTemplate());
            this.acsReferenceNumber = mergeValue(acsReferenceNumber, secureEcom.getAcsReferenceNumber());
            this.algorithm = mergeValue(algorithm, secureEcom.getAlgorithm());
            this.amount = mergeValue(amount, secureEcom.getAmount());
            this.authenticationSource = mergeValue(authenticationSource, secureEcom.getAuthenticationSource());
            this.authenticationType = mergeValue(authenticationType, secureEcom.getAuthenticationType());
            this.authenticationValue = mergeValue(authenticationValue, secureEcom.getAuthenticationValue());
            this.cardHolderResponseInfo = mergeValue(cardHolderResponseInfo, secureEcom.getCardHolderResponseInfo());
            this.cavv = mergeValue(cavv, secureEcom.getCavv());
            this.challengeMandated = mergeValue(challengeMandated, secureEcom.isChallengeMandated());
            this.challengeModel = mergeValue(challengeModel, secureEcom.getChallengeModel());
            this.redirectUrl = mergeValue(redirectUrl, secureEcom.getRedirectUrl());
            this.acsChallengeRequestUrl = mergeValue(acsChallengeRequestUrl, secureEcom.getAcsChallengeRequestUrl());
            this.methodUrl = mergeValue(methodUrl, secureEcom.getMethodUrl());
            this.threeDsServerTransId = mergeValue(threeDsServerTransId, secureEcom.getThreeDsServerTransId());
            this.threeDsMethodReturnUrl = mergeValue(threeDsMethodReturnUrl, secureEcom.getThreeDsMethodReturnUrl());
            this.encodedMethodData = mergeValue(encodedMethodData, secureEcom.getEncodedMethodData());
            this.messageExtensions = mergeValue(messageExtensions, secureEcom.getMessageExtensions());
            this.currency = mergeValue(currency, secureEcom.getCurrency());
            this.decoupledResponseIndicator = mergeValue(decoupledResponseIndicator, secureEcom.getDecoupledResponseIndicator());
            this.directoryServerTransactionId = mergeValue(directoryServerTransactionId, secureEcom.getDirectoryServerTransactionId());
            this.directoryServerEndVersion = mergeValue(directoryServerEndVersion, secureEcom.getDirectoryServerEndVersion());
            this.directoryServerStartVersion = mergeValue(directoryServerStartVersion, secureEcom.getDirectoryServerStartVersion());
            this.eci = mergeValue(eci, secureEcom.getEci());
            this.enrolled = mergeValue(enrolled, secureEcom.isEnrolled());
            this.enrolledStatus = mergeValue(enrolledStatus, secureEcom.getEnrolledStatus());
            this.issuerAcsUrl = mergeValue(issuerAcsUrl, secureEcom.getIssuerAcsUrl());
            this.messageCategory = mergeValue(messageCategory, secureEcom.getMessageCategory());
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
            this.liabilityShift = mergeValue(liabilityShift, secureEcom.getLiabilityShift());
            this.ucafIndicator = mergeValue(ucafIndicator, secureEcom.getUcafIndicator());
            //this.merchantData = mergeValue(merchantData, secureEcom.getMerchantData());
            this.providerServerTransRef = mergeValue(providerServerTransRef, secureEcom.getProviderServerTransRef());
            this.actionId = mergeValue(actionId, secureEcom.getActionId());
            this.actionType = mergeValue(actionType, secureEcom.getActionType());
            this.actionTimeCreated = mergeValue(actionTimeCreated, secureEcom.getActionTimeCreated());
            this.actionResultCode = mergeValue(actionResultCode, secureEcom.getActionResultCode());
            this.actionAppId = mergeValue(actionAppId, secureEcom.getActionAppId());
            this.actionAppName = mergeValue(actionAppName, secureEcom.getActionAppName());
            this.challengeWindowSize = mergeValue(challengeWindowSize, secureEcom.getChallengeWindowSize());
            this.deviceRenderingOptions = mergeValue(deviceRenderingOptions, secureEcom.getDeviceRenderingOptions());
            this.frictionlessFlowEligible = mergeValue(frictionlessFlowEligible, secureEcom.getFrictionlessFlowEligible());
            this.exemptionRequestType = mergeValue(exemptionRequestType, secureEcom.getExemptionRequestType());
            this.exemptionApprovalURL = mergeValue(exemptionApprovalURL, secureEcom.getExemptionApprovalURL());
            this.exemptionRejectionReason = mergeValue(exemptionRejectionReason, secureEcom.getExemptionRejectionReason());
            this.challengeType = mergeValue(challengeType, secureEcom.getChallengeType());
            this.creqData = mergeValue(creqData, secureEcom.getCreqData());
            this.acsSignedContent = mergeValue(acsSignedContent, secureEcom.getAcsSignedContent());
            this.decoupledFlowIndicator = mergeValue(decoupledFlowIndicator, secureEcom.getDecoupledFlowIndicator());
            this.decoupledFlowTimerValue = mergeValue(decoupledFlowTimerValue, secureEcom.getDecoupledFlowTimerValue());
            this.decoupledFlowTimerUnit = mergeValue(decoupledFlowTimerUnit, secureEcom.getDecoupledFlowTimerUnit());
            this.notificationURL = mergeValue(notificationURL, secureEcom.getNotificationURL());
            this.hardcodedDecision = mergeValue(hardcodedDecision, secureEcom.getHardcodedDecision());
            this.riskScore = mergeValue(riskScore, secureEcom.getRiskScore());
            this.riskAssessmentStatus = mergeValue(riskAssessmentStatus, secureEcom.getRiskAssessmentStatus());
            this.trustedMerchantExemption = mergeValue(trustedMerchantExemption, secureEcom.getTrustedMerchantExemption());
            this.lowValueExemption = mergeValue(lowValueExemption, secureEcom.getLowValueExemption());
            this.transactionRiskAnalysisExemption = mergeValue(transactionRiskAnalysisExemption, secureEcom.getTransactionRiskAnalysisExemption());
            this.recurringExemption = mergeValue(recurringExemption, secureEcom.getRecurringExemption());
            this.aggregationData = mergeValue(aggregationData, secureEcom.getAggregationData());
            this.acsRenderingType = mergeValue(acsRenderingType, secureEcom.getAcsRenderingType());
            this.methodUrlCompletionIndicator = mergeValue(methodUrlCompletionIndicator, secureEcom.getMethodUrlCompletionIndicator());
            this.threeRiRequestorPriorAuthenticationData = mergeValue(threeRiRequestorPriorAuthenticationData, secureEcom.getThreeRiRequestorPriorAuthenticationData());
            this.messageExtensionData = mergeValue(messageExtensionData, secureEcom.getMessageExtensionData());
            this.payerAuthenticationResponseDecrypted = mergeValue(payerAuthenticationResponseDecrypted, secureEcom.getPayerAuthenticationResponseDecrypted());
            this.authenticationRequestData = mergeValue(authenticationRequestData, secureEcom.getAuthenticationRequestData());
            this.authenticationResponseData = mergeValue(authenticationResponseData, secureEcom.getAuthenticationResponseData());
            this.paymentMethodResult = mergeValue(paymentMethodResult, secureEcom.getPaymentMethodResult());
            this.paymentMethodMessage = mergeValue(paymentMethodMessage, secureEcom.getPaymentMethodMessage());
            this.paymentMethodEntryMode = mergeValue(paymentMethodEntryMode, secureEcom.getPaymentMethodEntryMode());
            this.paymentMethodName = mergeValue(paymentMethodName, secureEcom.getPaymentMethodName());
            this.card = mergeValue(card, secureEcom.getCard());
        }
    }

    private <T> T mergeValue(T currentValue, T mergeValue) {
        if (mergeValue == null) {
            return currentValue;
        }
        return mergeValue;
    }
}
