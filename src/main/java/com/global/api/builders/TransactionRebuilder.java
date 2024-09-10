package com.global.api.builders;

import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.enums.TransactionTypeIndicator;
import com.global.api.network.entities.NtsData;
import com.global.api.network.enums.AuthorizerCode;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    private String originalTransactionDate;
    private boolean partialApproval;
    private PaymentMethodType paymentMethodType = PaymentMethodType.Credit;
    private String posDataCode;
    private String systemTraceAuditNumber;
    private String transactionId;
    private boolean useAuthorizedAmount;
    @Getter
    @Setter
    private TransactionType originalTransactionType;
    private String approvalCode;
    @Getter
    private AuthorizerCode authorizer;
    @Getter
    private String debitAuthorizer;
    @Getter
    private String banknetRefId;
    @Getter
    private String settlementDate;
    @Getter
    private String visaTransactionId;
    @Getter
    private String discoverNetworkRefId;
    @Getter
    private String originalMessageCode;
    @Getter
    private TransactionTypeIndicator transactionTypeIndicator;
    @Getter
    private Integer batchNumber;
    @Getter
    private Integer sequenceNumber;


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

    public TransactionRebuilder withAuthorizedAmount(BigDecimal value) {
        return withAuthorizedAmount(value, false);
    }

    public TransactionRebuilder withAuthorizedAmount(BigDecimal value, boolean useAuthorizedAmount) {
        originalApprovedAmount = value;
        this.useAuthorizedAmount = useAuthorizedAmount;
        return this;
    }

    public TransactionRebuilder withNtsData(NtsData value) {
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

    public TransactionRebuilder withOriginalTransactionType(TransactionType value) {
        originalTransactionType = value;
        return this;
    }


    public TransactionRebuilder withApprovalCode(String value) {
        this.approvalCode = value;
        return this;
    }

    public TransactionRebuilder withAuthorizer(AuthorizerCode value) {
        this.authorizer = value;
        return this;
    }

    public TransactionRebuilder withDebitAuthorizer(String value) {
        this.debitAuthorizer = value;
        return this;
    }

    public TransactionRebuilder withSettlementDate(String value) {
        this.settlementDate = value;
        return this;
    }

    public TransactionRebuilder withBanknetRefId(String value) {
        this.banknetRefId = value;
        return this;
    }

    public TransactionRebuilder withVisaTransactionId(String value) {
        this.visaTransactionId = value;
        return this;
    }

    public TransactionRebuilder withDiscoverNetworkRefId(String value) {
        this.discoverNetworkRefId = value;
        return this;
    }

    public TransactionRebuilder withOriginalTransactionDate(String value) {
        this.originalTransactionDate = value;
        return this;
    }

    public TransactionRebuilder withOriginalMessageCode(String value) {
        this.originalMessageCode = value;
        return this;
    }

    public TransactionRebuilder withTransactionTypeIndicator(TransactionTypeIndicator value) {
        this.transactionTypeIndicator = value;
        return this;
    }

    public TransactionRebuilder withBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
        return this;
    }

    public TransactionRebuilder withSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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
        reference.setUseAuthorizedAmount(useAuthorizedAmount);
        reference.setOriginalTransactionType(originalTransactionType);
        reference.setAuthorizer(authorizer);
        reference.setDebitAuthorizer(debitAuthorizer);
        reference.setOriginalTransactionDate(originalTransactionDate);
        reference.setApprovalCode(approvalCode);
        reference.setOriginalTransactionDate(originalTransactionDate);
        reference.setOriginalTransactionDate(originalTransactionDate);
        reference.setMastercardBanknetRefNo(banknetRefId);
        reference.setMastercardBanknetSettlementDate(settlementDate);
        reference.setVisaTransactionId(visaTransactionId);
        reference.setDiscoverNetworkRefId(discoverNetworkRefId);
        reference.setOriginalMessageCode(originalMessageCode);
        reference.setOriginalTransactionTypeIndicator(transactionTypeIndicator);
        if (batchNumber != null) {
            reference.setBatchNumber(batchNumber);
        }
        if (sequenceNumber != null) {
            reference.setSequenceNumber(sequenceNumber);
        }

        Transaction trans = new Transaction();
        trans.setTransactionReference(reference);

        return trans;
    }

}