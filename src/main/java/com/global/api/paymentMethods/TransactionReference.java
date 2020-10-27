package com.global.api.paymentMethods;

import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.network.entities.NtsData;

import java.math.BigDecimal;

public class TransactionReference implements IPaymentMethod {
	private String alternativePaymentType;
	private String acquiringInstitutionId;
    private String authCode;
    private Integer batchNumber;
    private String clientTransactionId;
    private String messageTypeIndicator;
    private NtsData ntsData;
    private String orderId;
    private BigDecimal originalAmount;
    private BigDecimal originalApprovedAmount;
    private EmvChipCondition originalEmvChipCondition;
    private IPaymentMethod originalPaymentMethod;
    private String originalProcessingCode;
    private String originalTransactionTime;
    private boolean partialApproval;
    private PaymentMethodType paymentMethodType;
    private String posDataCode;
    private int sequenceNumber;
    private String systemTraceAuditNumber;
    private String transactionId;

    public String getAlternativePaymentType() {
		return alternativePaymentType;
	}
	public void setAlternativePaymentType(String alternativePaymentType) {
		this.alternativePaymentType = alternativePaymentType;
	}

    public String getAcquiringInstitutionId() {
        return acquiringInstitutionId;
    }
    public void setAcquiringInstitutionId(String acquiringInstitutionId) {
        this.acquiringInstitutionId = acquiringInstitutionId;
    }

    public String getAuthCode() {
        return authCode;
    }
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }
    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getClientTransactionId() {
        return clientTransactionId;
    }
    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }

    public String getMessageTypeIndicator() {
        return messageTypeIndicator;
    }
    public void setMessageTypeIndicator(String messageTypeIndicator) {
        this.messageTypeIndicator = messageTypeIndicator;
    }

    public NtsData getNtsData() {
        return ntsData;
    }
    public void setNtsData(NtsData ntsData) {
        this.ntsData = ntsData;
    }
    public void setNtsData(String ntsData) {
        this.ntsData = NtsData.fromString(ntsData);
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }
    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getOriginalApprovedAmount() {
        if(originalApprovedAmount != null) {
            return originalApprovedAmount;
        }
        return originalAmount;
    }
    public void setOriginalApprovedAmount(BigDecimal originalApprovedAmount) {
        this.originalApprovedAmount = originalApprovedAmount;
    }

    public EmvChipCondition getOriginalEmvChipCondition() {
        return originalEmvChipCondition;
    }
    public void setOriginalEmvChipCondition(EmvChipCondition originalEmvChipCondition) {
        this.originalEmvChipCondition = originalEmvChipCondition;
    }

    public IPaymentMethod getOriginalPaymentMethod() {
        return originalPaymentMethod;
    }
    public void setOriginalPaymentMethod(IPaymentMethod originalPaymentMethod) {
        this.originalPaymentMethod = originalPaymentMethod;
    }

    public String getOriginalProcessingCode() {
        return originalProcessingCode;
    }
    public void setOriginalProcessingCode(String originalProcessingCode) {
        this.originalProcessingCode = originalProcessingCode;
    }

    public String getOriginalTransactionTime() {
        return originalTransactionTime;
    }
    public void setOriginalTransactionTime(String originalTransactionTime) {
        this.originalTransactionTime = originalTransactionTime;
    }


    public boolean isPartialApproval() {
        return this.partialApproval;
    }
    public void setPartialApproval(boolean partial) {
        this.partialApproval = partial;
    }

    public PaymentMethodType getPaymentMethodType() {
        if(originalPaymentMethod != null) {
            return originalPaymentMethod.getPaymentMethodType();
        }
        return paymentMethodType;
    }
    public void setPaymentMethodType(PaymentMethodType paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public String getPosDataCode() {
        return posDataCode;
    }
    public void setPosDataCode(String posDataCode) {
        this.posDataCode = posDataCode;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }
    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
