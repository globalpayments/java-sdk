package com.global.api.entities;

import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionRebuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.network.entities.gnap.GnapResponse;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsResponse;
import com.global.api.network.enums.CardIssuerEntryTag;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class Transaction {
    private String additionalResponseCode;
    private BigDecimal authorizedAmount;
    @Getter @Setter private String autoSettleFlag;
    private BigDecimal availableBalance;
    private String avsResponseCode;
    private String avsResponseMessage;
    public String avsAddressResponse;
    private BigDecimal balanceAmount;
    private BatchSummary batchSummary;
    private String cardBrandTransactionId;
    private AlternativePaymentResponse alternativePaymentResponse;
    private String cardType;
    private String cardLast4;
    @Getter @Setter private String fingerPrint;
    @Getter @Setter private String fingerPrintIndicator;
    private String cardNumber;
    private int cardExpMonth;
    private int cardExpYear;
    private String cavvResponseCode;
    private String commercialIndicator;
    private BigDecimal convenienceFee;
    private String cvnResponseCode;
    private String cvnResponseMessage;
    private DccRateData dccRateData;
    private DebitMac debitMac;
    private String emvIssuerResponse;
    @Getter @Setter private FraudResponse fraudResponse;
    private LinkedList<IGatewayEvent> gatewayEvents;
    private Date hostResponseDate;
    @Getter @Setter private boolean multiCapture;
    @Getter @Setter private Integer multiCapturePaymentCount;
    @Getter @Setter private Integer multiCaptureSequence;
    private HashMap<CardIssuerEntryTag, String> issuerData;
    private PriorMessageInformation messageInformation;
    private BigDecimal pointsBalanceAmount;
    private Transaction preAuthCompletion;
    private String recurringDataCode;
    private String referenceNumber;
    private String responseCode;
    private Date responseDate;
    private String responseMessage;
    private HashMap<String, String> responseValues;
    private String schemeId;
    private ThreeDSecure threeDsecure;
    private String timestamp;
    private String transactionDescriptor;
    private String transactionToken;
    private String token;
    @Getter @Setter private PaymentMethodUsageMode tokenUsageMode;
    private GiftCard giftCard;
    private TransactionReference transactionReference;
    @Getter
    @Setter
    private GnapResponse gnapResponse;
    private NtsResponse ntsResponse;
    private String transactionDate;
    private String transactionTime;
    private String transactionCode;
    @Getter @Setter private AdditionalDuplicateData additionalDuplicateData;

    public BigDecimal getOrigionalAmount() {
        return origionalAmount;
    }

    public void setOrigionalAmount(BigDecimal origionalAmount) {
        this.origionalAmount = origionalAmount;
    }

    private BigDecimal origionalAmount;
    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }
    public String getTransactionCode() {
        return transactionCode;
    }
    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public NtsResponse getNtsResponse() {
        return ntsResponse;
    }

    public void setNtsResponse(NtsResponse ntsResponse) {
        this.ntsResponse = ntsResponse;
    }
    public String getAdditionalResponseCode() {
        return additionalResponseCode;
    }
    public void setAdditionalResponseCode(String additionalResponseCode) {
        this.additionalResponseCode = additionalResponseCode;
    }
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
    public String getAvsAddressResponse() {
        return avsAddressResponse;
    }
    public void setAvsAddressResponse(String avsAddressResponse) {
        this.avsAddressResponse = avsAddressResponse;
    }
    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }
    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }
    public Integer getBatchId() {
        if(transactionReference != null) {
            return transactionReference.getBatchNumber();
        }
        return null;
    }
    public BatchSummary getBatchSummary() {
        return batchSummary;
    }
    public void setBatchSummary(BatchSummary batchSummary) {
        this.batchSummary = batchSummary;
    }
    public String getCardBrandTransactionId() {
        return cardBrandTransactionId;
    }
    public void setCardBrandTransactionId(String cardBrandTxnId) {
        this.cardBrandTransactionId = cardBrandTxnId;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public AlternativePaymentResponse getAlternativePaymentResponse() {
        return transactionReference.getAlternativePaymentResponse();
    }
    public void setAlternativePaymentResponse(AlternativePaymentResponse value) {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setAlternativePaymentResponse(value);
    }
    public String getCardLast4() {
        return cardLast4;
    }
    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }
    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    public int getCardExpMonth() {
        return cardExpMonth;
    }
    public void setCardExpMonth(int cardExpMonth) {
        this.cardExpMonth = cardExpMonth;
    }
    public int getCardExpYear() {
        return cardExpYear;
    }
    public void setCardExpYear(int cardExpYear) {
        this.cardExpYear = cardExpYear;
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
    public BigDecimal getConvenienceFee() {
        return convenienceFee;
    }
    public void setConvenienceFee(BigDecimal convenienceFee) {
        this.convenienceFee = convenienceFee;
    }
    public String getCvnResponseCode() {
        return cvnResponseCode;
    }
    public void setCvnResponseCode(String cvnResponseCode) {
        this.cvnResponseCode = cvnResponseCode;
    }
    public Date getResponseDate() {
        return responseDate;
    }
    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }
    public String getCvnResponseMessage() {
        return cvnResponseMessage;
    }
    public void setCvnResponseMessage(String cvnResponseMessage) {
        this.cvnResponseMessage = cvnResponseMessage;
    }
    public DccRateData getDccRateData() {
        return dccRateData;
    }
    public void setDccRateData(DccRateData dccRateData) {
        this.dccRateData = dccRateData;
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
    public HashMap<CardIssuerEntryTag, String> getIssuerData() {
        return issuerData;
    }
    public void setIssuerData(CardIssuerEntryTag tag, String value) {
        if(issuerData == null) {
            issuerData = new HashMap<CardIssuerEntryTag, String>();
        }
        this.issuerData.put(tag, value);
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
    public void setNtsData(NtsData value) throws GatewayException {
        if(transactionReference == null) {
            transactionReference = new TransactionReference();
        }
        transactionReference.setNtsData(value.toString());
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
    public Transaction getPreAuthCompletion() {
        return preAuthCompletion;
    }
    public void setPreAuthCompletion(Transaction preAuthCompletion) {
        this.preAuthCompletion = preAuthCompletion;
    }
    public BigDecimal getPointsBalanceAmount() {
        return pointsBalanceAmount;
    }
    public void setPointsBalanceAmount(BigDecimal pointsBalanceAmount) {
        this.pointsBalanceAmount = pointsBalanceAmount;
    }
    public String getPosDataCode() {
        if(transactionReference != null) {
            return transactionReference.getPosDataCode();
        }
        return null;
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
    public String getSchemeId() {
        return schemeId;
    }
    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
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
    public ManagementBuilder capture(double amount) {
        return capture(new BigDecimal(amount));
    }
    public ManagementBuilder capture(BigDecimal amount) {
        ManagementBuilder builder =
                new ManagementBuilder(TransactionType.Capture)
                        .withPaymentMethod(transactionReference)
                        .withAmount(amount);

        if (isMultiCapture()) {
            builder.withMultiCapture(getMultiCaptureSequence(), getMultiCapturePaymentCount());
        }

        return builder;
    }

    public ManagementBuilder cancel() {
        return cancel(null);
    }
    public ManagementBuilder cancel(BigDecimal amount) {
        TransactionType transType = TransactionType.Void;
        if(transactionReference != null && transactionReference.getPaymentMethodType().equals(PaymentMethodType.Debit)) {
            transType = TransactionType.Reversal;
        }

        return new ManagementBuilder(transType)
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

    // Confirm an original transaction. For now on, it is used for the APM transactions with PayPal
    public ManagementBuilder confirm() {
        return confirm(null);
    }

    public ManagementBuilder confirm(BigDecimal amount) {
        return
                new ManagementBuilder(TransactionType.Confirm)
                        .withPaymentMethod(this.transactionReference)
                        .withAmount(amount);
    }

    public ManagementBuilder increment() {
        return increment(null);
    }
    public ManagementBuilder increment(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Increment)
                .withAmount(amount)
                .withPaymentMethod(transactionReference);
    }

    public ManagementBuilder preAuthCompletion() {
        return preAuthCompletion(null);
    }
    public ManagementBuilder preAuthCompletion(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.PreAuthCompletion)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }

    public ManagementBuilder refund() {
        return refund(null);
    }
    public ManagementBuilder refund(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Refund)
                .withPaymentMethod(transactionReference)
                .withAmount(amount);
    }
    public ManagementBuilder refund(double amount) {
        return refund(new BigDecimal(amount));
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
    public ManagementBuilder reverse(double amount) {
        return reverse(new BigDecimal(amount));
    }

    public ManagementBuilder reauthorize() {
        return reauthorize(null);
    }
    public ManagementBuilder reauthorize(BigDecimal amount) {
        return new ManagementBuilder(TransactionType.Reauth)
                .withPaymentMethod(transactionReference)
                .withAmount(amount != null ? amount : balanceAmount);
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
                .withForceToHost(force);
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
        return new TransactionRebuilder()
                .withTransactionId(transactionId)
                .withOrderId(orderId)
                .withPaymentMethodType(paymentMethodType)
                .build();
    }

    public static Transaction fromId(String transactionId, PaymentMethodType paymentMethodType, IPaymentMethod paymentMethod) {
        return new TransactionRebuilder()
                .withTransactionId(transactionId)
                .withPaymentMethod(paymentMethod)
                .withPaymentMethodType(paymentMethodType)
                .build();
    }


    public static Transaction fromClientTransactionId(String clientTransactionId) {
        return fromClientTransactionId(clientTransactionId, null, PaymentMethodType.Credit);
    }
    public static Transaction fromClientTransactionId(String clientTransactionId, PaymentMethodType paymentMethodType) {
        return fromClientTransactionId(clientTransactionId, null, paymentMethodType);
    }
    public static Transaction fromClientTransactionId(String clientTransactionId, String orderId) {
        return fromClientTransactionId(clientTransactionId, orderId, PaymentMethodType.Credit);
    }
    public static Transaction fromClientTransactionId(String clientTransactionId, String orderId, PaymentMethodType paymentMethodType) {
        TransactionReference reference = new TransactionReference();
        reference.setClientTransactionId(clientTransactionId);
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
        return new TransactionRebuilder()
                .withAmount(amount)
                .withAuthorizationCode(authCode)
                .withNtsData(originalNtsCode)
                .withPaymentMethod(originalPaymentMethod)
                .withMessageTypeIndicator(messageTypeIndicator)
                .withSystemTraceAuditNumber(stan)
                .withTransactionTime(originalTransactionTime)
                .withProcessingCode(originalProcessingCode)
                .withAcquirerId(acquirerId)
                .build();
    }

    public static TransactionRebuilder fromBuilder() {
        return new TransactionRebuilder();
    }
}