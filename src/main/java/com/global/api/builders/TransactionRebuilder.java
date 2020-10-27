package com.global.api.builders;

import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.network.entities.NtsData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;

import java.math.BigDecimal;

public class TransactionRebuilder {
    private String authCode;
    private String acquirerId;
    private String messageTypeIndicator;
    private String orderId;
    private BigDecimal originalAmount;
    private BigDecimal originalApprovedAmount;
    private NtsData originalNtsData;
    private IPaymentMethod originalPaymentMethod;
    private String originalProcessingCode;
    private String originalTransactionTime;
    private boolean partialApproval;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Credit;
    private String posDataCode;
    private String systemTraceAuditNumber;
    private String transactionId;

    public TransactionRebuilder withAuthorizationCode(String value) {
        authCode = value;
        return this;
    }
    public TransactionRebuilder withAcquirerId(String value) {
        acquirerId = value;
        return this;
    }
    public TransactionRebuilder withMessageTypeIndicator(String value) {
        messageTypeIndicator = value;
        return this;
    }
    public TransactionRebuilder withOrderId(String value) {
        orderId = value;
        return this;
    }
    public TransactionRebuilder withAmount(BigDecimal value) {
        originalAmount = value;
        return this;
    }
    public TransactionRebuilder withAuthorizedAmount (BigDecimal value) {
        return withAuthorizedAmount(value, false);
    }
    public TransactionRebuilder withAuthorizedAmount (BigDecimal value, boolean useAuthorizedAmount) {
        originalApprovedAmount = value;
        partialApproval = useAuthorizedAmount;
        return this;
    }
    public TransactionRebuilder withNtsData (NtsData value) {
        originalNtsData = value;
        return this;
    }
    public TransactionRebuilder withPartialApproval(boolean value) {
        partialApproval = value;
        return this;
    }
    public TransactionRebuilder withPaymentMethod(IPaymentMethod value) {
        originalPaymentMethod = value;
        return this;
    }
    public TransactionRebuilder withProcessingCode(String value) {
        originalProcessingCode = value;
        return this;
    }
    public TransactionRebuilder withPosDataCode(String value) {
        posDataCode = value;
        return this;
    }
    public TransactionRebuilder withTransactionTime(String value) {
        originalTransactionTime = value;
        return this;
    }
    public TransactionRebuilder withPaymentMethodType(PaymentMethodType value) {
        paymentMethodType = value;
        return this;
    }
    public TransactionRebuilder withSystemTraceAuditNumber(String value) {
        systemTraceAuditNumber = value;
        return this;
    }
    public TransactionRebuilder withTransactionId(String value) {
        transactionId = value;
        return this;
    }

    public Transaction build() {
        TransactionReference reference = new TransactionReference();
        reference.setAcquiringInstitutionId(acquirerId);
        reference.setAuthCode(authCode);
        reference.setMessageTypeIndicator(messageTypeIndicator);
        reference.setNtsData(originalNtsData);
        reference.setOriginalAmount(originalAmount);
        reference.setOrderId(orderId);
        reference.setOriginalApprovedAmount(originalApprovedAmount);
        reference.setOriginalPaymentMethod(originalPaymentMethod);
        reference.setOriginalProcessingCode(originalProcessingCode);
        reference.setOriginalTransactionTime(originalTransactionTime);
        reference.setPartialApproval(partialApproval);
        reference.setPaymentMethodType(paymentMethodType);
        reference.setPosDataCode(posDataCode);
        reference.setSystemTraceAuditNumber(systemTraceAuditNumber);
        reference.setTransactionId(transactionId);

        Transaction trans = new Transaction();
        trans.setTransactionReference(reference);

        return trans;
    }
}
