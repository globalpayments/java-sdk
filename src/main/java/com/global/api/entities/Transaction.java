package com.global.api.entities;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;

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
    private String emvIssuerResponse;
    private BigDecimal pointsBalanceAmount;
    private String recurringDataCode;
    private String referenceNumber;
    private String responseCode;
    private String responseMessage;
    private String timestamp;
    private String transactionDescriptor;
    private String token;
    private GiftCard giftCard;
    private TransactionReference transactionReference;

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
    public String getEmvIssuerResponse() {
        return emvIssuerResponse;
    }
    public void setEmvIssuerResponse(String emvIssuerResponse) {
        this.emvIssuerResponse = emvIssuerResponse;
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
    public void setCommercialIndicator(String commercialIndicator) {
        this.commercialIndicator = commercialIndicator;
    }
    public BigDecimal getPointsBalanceAmount() {
        return pointsBalanceAmount;
    }
    public void setPointsBalanceAmount(BigDecimal pointsBalanceAmount) {
        this.pointsBalanceAmount = pointsBalanceAmount;
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

    public ManagementBuilder edit() {
        return new ManagementBuilder(TransactionType.Edit)
                .withPaymentMethod(transactionReference);
    }

    public ManagementBuilder hold() {
        return new ManagementBuilder(TransactionType.Hold).withPaymentMethod(transactionReference);
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
        return new ManagementBuilder(TransactionType.Void).withPaymentMethod(transactionReference);
    }

    public static Transaction fromId(String transactionId) {
        return fromId(transactionId, PaymentMethodType.Credit);
    }
    public static Transaction fromId(String transactionId, PaymentMethodType paymentMethodType) {
        return fromId(transactionId, paymentMethodType);
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
}
