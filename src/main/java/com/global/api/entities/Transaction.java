package com.global.api.entities;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Transaction {
    private BigDecimal authorizedAmount;
    private BigDecimal availableBalance;
    private String avsResponseCode;
    private String avsResponseMessage;
    private BigDecimal balanceAmount;
    private BatchSummary batchSummary;
    private String cardType;
    private String cardLast4;
    private String cavvResponseCode;
    private String commercialIndicator;
    private String cvnResponseCode;
    private String cvnResponseMessage;
    private DccResponseResult dccResponseResult;
    private DebitMac debitMac;
    private String emvIssuerResponse;
    private LinkedList<IGatewayEvent> gatewayEvents;
    private Date hostResponseDate;
    private PriorMessageInformation messageInformation;
    private String multiCapture;
    private BigDecimal pointsBalanceAmount;
    private String recurringDataCode;
    private String referenceNumber;
    private String responseCode;
    private String responseMessage;
    private HashMap<String, String> responseValues;
    private ThreeDSecure threeDsecure;
    private String timestamp;
    private String transactionDescriptor;
    private String transactionToken;
    private String token;
    private GiftCard giftCard;
    private TransactionReference transactionReference;

    public String getAcquiringInstitutionId() {
        if(transactionReference != null) {
            return transactionReference.getAcquiringInstitutionId();
        }
        return null;
    }
    public void setAcquiringInstitutionId(String value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setAcquiringInstitutionId(value);
    }
    public BigDecimal getAuthorizedAmount() {
        return authorizedAmount;
    }
    public void setAuthorizedAmount(BigDecimal authorizedAmount) {
        this.authorizedAmount = authorizedAmount;
    }
    public String getAuthorizationCode() {
        if(transactionReference != null)
            return transactionReference.getAuthCode();
        return null;
    }
    public void setAuthorizationCode(String value) {
        if(transactionReference == null)
            this.transactionReference = new TransactionReference();
        transactionReference.setAuthCode(value);
    }
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
    public String getAvsResponseCode() {
        return avsResponseCode;
    }
    public void setAvsResponseCode(String avsResponseCode) {
        this.avsResponseCode = avsResponseCode;
    }
    public String getAvsResponseMessage() {
        return avsResponseMessage;
    }
    public void setAvsResponseMessage(String avsResponseMessage) {
        this.avsResponseMessage = avsResponseMessage;
    }
    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }
    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }
    public BatchSummary getBatchSummary() {
        return batchSummary;
    }
    public void setBatchSummary(BatchSummary batchSummary) {
        this.batchSummary = batchSummary;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public String getCardLast4() {
        return cardLast4;
    }
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    public String getCavvResponseCode() {
        return cavvResponseCode;
    }
    public void setCavvResponseCode(String cavvResponseCode) {
        this.cavvResponseCode = cavvResponseCode;
    }
    public String getClientTransactionId() {
        if(transactionReference != null)
            return transactionReference.getClientTransactionId();
        return null;
    }
    public void setClientTransactionId(String clientTransactionId) {
        if(transactionReference == null)
            transactionReference = new TransactionReference();
        this.transactionReference.setClientTransactionId(clientTransactionId);
    }
    public String getCommercialIndicator() {
        return commercialIndicator;
    }
    public void setCommercialIndicator(String commercialIndicator) {
        this.commercialIndicator = commercialIndicator;
    }
    public String getCvnResponseCode() {
        return cvnResponseCode;
    }
    public void setCvnResponseCode(String cvnResponseCode) {
        this.cvnResponseCode = cvnResponseCode;
    }
    public String getCvnResponseMessage() {
        return cvnResponseMessage;
    }
    public void setCvnResponseMessage(String cvnResponseMessage) {
        this.cvnResponseMessage = cvnResponseMessage;
    }
    public DccResponseResult getDccResponseResult() {
		return dccResponseResult;
	}
	public void setDccResponseResult(DccResponseResult dccResponseResult) {
		this.dccResponseResult = dccResponseResult;
	}
	public DebitMac getDebitMac() {
        return debitMac;
    }
    public void setDebitMac(DebitMac debitMac) {
        this.debitMac = debitMac;
    }
    public LinkedList<IGatewayEvent> getGatewayEvents() {
        return gatewayEvents;
    }
    public void setGatewayEvents(LinkedList<IGatewayEvent> gatewayEvents) {
        this.gatewayEvents = gatewayEvents;
    }
    public Date getHostResponseDate() {
        return hostResponseDate;
    }
    public void setHostResponseDate(Date hostResponseDate) {
        this.hostResponseDate = hostResponseDate;
    }
    public PriorMessageInformation getMessageInformation() {
        return this.messageInformation;
    }
    public void setMessageInformation(PriorMessageInformation value) {
        this.messageInformation = value;
    }
    public String getMessageTypeIndicator() {
        if(transactionReference != null) {
            return transactionReference.getMessageTypeIndicator();
        }
        return null;
    }
    public void setMessageTypeIndicator(String value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setMessageTypeIndicator(value);
    }
    public String getMultiCapture() {
		return multiCapture;
	}
    public void setMultiCapture(String multiCapture) {
    	this.multiCapture = multiCapture;
	}
	public HashMap<String, String> getResponseValues() {
        return responseValues;
    }
    public void setResponseValues(HashMap<String, String> responseValues) {
        this.responseValues = responseValues;
    }
    public String getEmvIssuerResponse() {
        return emvIssuerResponse;
    }
    public void setEmvIssuerResponse(String emvIssuerResponse) {
        this.emvIssuerResponse = emvIssuerResponse;
    }
    public NtsData getNtsData() {
        if(transactionReference != null) {
            return transactionReference.getNtsData();
        }
        return null;
    }
    public void setNtsData(NtsData value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setNtsData(value);
    }
    public String getOrderId() {
        if(transactionReference != null)
            return transactionReference.getOrderId();
        return null;
    }
    public void setOrderId(String value) {
        if(transactionReference == null)
            transactionReference = new TransactionReference();
        transactionReference.setOrderId(value);
    }
    public String getOriginalTransactionTime() {
        if(transactionReference != null) {
            return transactionReference.getOriginalTransactionTime();
        }
        return null;
    }
    public void setOriginalTransactionTime(String value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setOriginalTransactionTime(value);
    }
    public PaymentMethodType getPaymentMethodType() {
        if(transactionReference != null)
            return transactionReference.getPaymentMethodType();
        return null;
    }
    public void setPaymentMethodType(PaymentMethodType value) {
        if(transactionReference == null)
            transactionReference = new TransactionReference();
        transactionReference.setPaymentMethodType(value);
    }
    public BigDecimal getPointsBalanceAmount() {
        return pointsBalanceAmount;
    }
    public void setPointsBalanceAmount(BigDecimal pointsBalanceAmount) {
        this.pointsBalanceAmount = pointsBalanceAmount;
    }
    public String getProcessingCode() {
        if(transactionReference != null) {
            return transactionReference.getOriginalProcessingCode();
        }
        return null;
    }
    public void setProcessingCode(String value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setOriginalProcessingCode(value);
    }
    public String getRecurringDataCode() {
        return recurringDataCode;
    }
    public void setRecurringDataCode(String recurringDataCode) {
        this.recurringDataCode = recurringDataCode;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public String getResponseMessage() {
        return responseMessage;
    }
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    public String getSystemTraceAuditNumber() {
        if(transactionReference != null) {
            return transactionReference.getSystemTraceAuditNumber();
        }
        return null;
    }
    public void setSystemTraceAuditNumber(String value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setSystemTraceAuditNumber(value);
    }
    public ThreeDSecure getThreeDsecure() {
        return threeDsecure;
    }
    public void setThreeDsecure(ThreeDSecure threeDsecure) {
        this.threeDsecure = threeDsecure;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getTransactionDescriptor() {
        return transactionDescriptor;
    }
    public void setTransactionDescriptor(String transactionDescriptor) {
        this.transactionDescriptor = transactionDescriptor;
    }
    public String getTransactionToken() {
        return transactionToken;
    }
    public void setTransactionToken(String transactionToken) {
        this.transactionToken = transactionToken;
    }
    public String getTransactionId() {
        if(transactionReference != null)
            return transactionReference.getTransactionId();
        return null;
    }
    public void setTransactionId(String value) {
        if(transactionReference == null)
            transactionReference = new TransactionReference();
        transactionReference.setTransactionId(value);
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public GiftCard getGiftCard() {
        return giftCard;
    }
    public void setGiftCard(GiftCard giftCard) {
        this.giftCard = giftCard;
    }
    public TransactionReference getTransactionReference() {
        return transactionReference;
    }
    public void setTransactionReference(TransactionReference transactionReference) {
        this.transactionReference = transactionReference;
    }

    public ManagementBuilder additionalAuth() {
        return additionalAuth(null);
    }
    public ManagementBuilder additionalAuth(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Auth)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }

    public ManagementBuilder capture() {
        return capture(null);
    }
    public ManagementBuilder capture(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Capture)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }
    
    public ManagementBuilder multicapture(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Multicapture)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }
    
    public ManagementBuilder cancel() {
        return cancel(null);
    }
    public ManagementBuilder cancel(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Void)
                .withPaymentMethod(transactionReference)
                .withCustomerInitiated(true)
                .withAmount(amount);
    }

    public ManagementBuilder edit() {
        return new ManagementBuilder(TransactionType.Edit)
                .withPaymentMethod(transactionReference);
    }

    public ManagementBuilder hold() {
        return new ManagementBuilder(TransactionType.Hold).withPaymentMethod(transactionReference);
    }

    public ManagementBuilder increment() {
        return increment(null);
    }
    public ManagementBuilder increment(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Increment)
                .withAmount(amount)
                .withPaymentMethod(transactionReference);
    }

    public ManagementBuilder refund() {
        return refund(null);
    }
    public ManagementBuilder refund(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Refund)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }

    public ManagementBuilder release() {
        return new ManagementBuilder(TransactionType.Release).withPaymentMethod(transactionReference);
    }

    public ManagementBuilder reverse() {
        return reverse(null);
    }
    public ManagementBuilder reverse(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Reversal)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }

    public ManagementBuilder voidTransaction() {
        return voidTransaction(null, false);
    }
    public ManagementBuilder voidTransaction(BigDecimal amount) {
        return voidTransaction(amount, false);
    }
    public ManagementBuilder voidTransaction(boolean force) {
        return voidTransaction(null, force);
    }
    public ManagementBuilder voidTransaction(BigDecimal amount, boolean force) {
        return new ManagementBuilder(TransactionType.Void)
                .withAmount(amount)
                .withPaymentMethod(transactionReference)
                .withForcedReversal(force);
    }

    public static Transaction fromId(String transactionId) {
        return fromId(transactionId, null, PaymentMethodType.Credit);
    }
    public static Transaction fromId(String transactionId, PaymentMethodType paymentMethodType) {
        return fromId(transactionId, null, paymentMethodType);
    }
    public static Transaction fromId(String transactionId, String orderId) {
        return fromId(transactionId, orderId, PaymentMethodType.Credit);
    }
    public static Transaction fromId(String transactionId, String orderId, PaymentMethodType paymentMethodType) {
        TransactionReference reference = new TransactionReference();
        reference.setTransactionId(transactionId);
        reference.setOrderId(orderId);
        reference.setPaymentMethodType(paymentMethodType);

        Transaction trans = new Transaction();
        trans.setTransactionReference(reference);

        return trans;
    }

    public static Transaction fromNetwork(BigDecimal amount, String authCode, NtsData originalNtsCode, IPaymentMethod originalPaymentMethod) {
        return fromNetwork(amount, authCode, originalNtsCode, originalPaymentMethod, null);
    }
    public static Transaction fromNetwork(BigDecimal amount, String authCode, NtsData originalNtsCode, IPaymentMethod originalPaymentMethod, String originalProcessingCode) {
        return fromNetwork(amount, authCode, originalNtsCode, originalPaymentMethod, null, null, null, originalProcessingCode);
    }
    public static Transaction fromNetwork(BigDecimal amount, String authCode, NtsData originalNtsCode, IPaymentMethod originalPaymentMethod, String messageTypeIndicator, String stan, String originalTransactionTime) {
        return fromNetwork(amount, authCode, originalNtsCode, originalPaymentMethod, messageTypeIndicator, stan, originalTransactionTime, null);
    }
    public static Transaction fromNetwork(BigDecimal amount, String authCode, NtsData originalNtsCode, IPaymentMethod originalPaymentMethod, String messageTypeIndicator, String stan, String originalTransactionTime, String originalProcessingCode) {
        return fromNetwork(amount, authCode, originalNtsCode, originalPaymentMethod, messageTypeIndicator, stan, originalTransactionTime, originalProcessingCode, null);
    }
    public static Transaction fromNetwork(BigDecimal amount, String authCode, NtsData originalNtsCode, IPaymentMethod originalPaymentMethod, String messageTypeIndicator, String stan, String originalTransactionTime, String originalProcessingCode, String acquirerId) {
        TransactionReference reference = new TransactionReference();
        reference.setOriginalAmount(amount);
        reference.setAcquiringInstitutionId(acquirerId);
        reference.setAuthCode(authCode);
        reference.setMessageTypeIndicator(messageTypeIndicator);
        reference.setNtsData(originalNtsCode);
        reference.setOriginalPaymentMethod(originalPaymentMethod);
        reference.setOriginalTransactionTime(originalTransactionTime);
        reference.setSystemTraceAuditNumber(stan);
        reference.setOriginalProcessingCode(originalProcessingCode);

        Transaction trans = new Transaction();
        trans.setTransactionReference(reference);

        return trans;
    }
}
