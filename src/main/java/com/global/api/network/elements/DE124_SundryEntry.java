package com.global.api.network.elements;

import com.global.api.network.enums.DE124_SundryDataTag;

import java.math.BigDecimal;

public class DE124_SundryEntry {
    private DE124_SundryDataTag tag;
    private String customerData;

    private String primaryAccountNumber; // DE2
    private DE3_ProcessingCode processingCode; // DE3
    private BigDecimal transactionAmount; // DE4
    private String systemTraceAuditNumber; // DE11
    private String transactionLocalDateTime; // DE12
    private String expirationDate; // DE14
    private DE22_PosDataCode posDataCode; // DE22
    private String functionCode; // DE24
    private String messageReasonCode; //DE25
    private String approvalCode; // DE38
    private String batchNumber; // DE48-4
    private String cardType; // DE48-11
    private String messageTypeIndicator; // DE56.1
    private String originalStan; // DE56.2
    private String originalDateTime; // DE56.3
    private DE62_CardIssuerData cardIssuerData; // DE62
    private DE63_ProductData productData; // DE63

    public DE124_SundryDataTag getTag() {
        return tag;
    }
    public void setTag(DE124_SundryDataTag tag) {
        this.tag = tag;
    }
    public String getCustomerData() {
        return customerData;
    }
    public void setCustomerData(String customerData) {
        this.customerData = customerData;
    }
    public String getPrimaryAccountNumber() {
        return primaryAccountNumber;
    }
    public void setPrimaryAccountNumber(String primaryAccountNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
    }
    public DE3_ProcessingCode getProcessingCode() {
        return processingCode;
    }
    public void setProcessingCode(DE3_ProcessingCode processingCode) {
        this.processingCode = processingCode;
    }
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }
    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }
    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }
    public String getTransactionLocalDateTime() {
        return transactionLocalDateTime;
    }
    public void setTransactionLocalDateTime(String transactionLocalDateTime) {
        this.transactionLocalDateTime = transactionLocalDateTime;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }
    public DE22_PosDataCode getPosDataCode() {
        return posDataCode;
    }
    public void setPosDataCode(DE22_PosDataCode posDataCode) {
        this.posDataCode = posDataCode;
    }
    public String getFunctionCode() {
        return functionCode;
    }
    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }
    public String getMessageReasonCode() {
        return messageReasonCode;
    }
    public void setMessageReasonCode(String messageReasonCode) {
        this.messageReasonCode = messageReasonCode;
    }
    public String getApprovalCode() {
        return approvalCode;
    }
    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }
    public String getBatchNumber() {
        return batchNumber;
    }
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public String getMessageTypeIndicator() {
        return messageTypeIndicator;
    }
    public void setMessageTypeIndicator(String messageTypeIndicator) {
        this.messageTypeIndicator = messageTypeIndicator;
    }
    public String getOriginalStan() {
        return originalStan;
    }
    public void setOriginalStan(String originalStan) {
        this.originalStan = originalStan;
    }
    public String getOriginalDateTime() {
        return originalDateTime;
    }
    public void setOriginalDateTime(String originalDateTime) {
        this.originalDateTime = originalDateTime;
    }
    public DE62_CardIssuerData getCardIssuerData() {
        return cardIssuerData;
    }
    public void setCardIssuerData(DE62_CardIssuerData cardIssuerData) {
        this.cardIssuerData = cardIssuerData;
    }
    public DE63_ProductData getProductData() {
        return productData;
    }
    public void setProductData(DE63_ProductData productData) {
        this.productData = productData;
    }
}
