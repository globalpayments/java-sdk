package com.global.api.entities;

import java.math.BigDecimal;
import java.util.Date;

public class TransactionSummary {
    private BigDecimal amount;
    private String authCode;
    private BigDecimal authorizedAmount;
    private String clientTransactionId;
    private BigDecimal convenienceAmount;
    private int deviceId;
    private String issuerResponseCode;
    private String issuerResponseMessage;
    private String maskedCardNumber;
    private String originalTransactionId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String referenceNumber;
    private String serviceName;
    private BigDecimal settlementAmount;
    private BigDecimal shippingAmount;
    private String status;
    private Date transactionDate;
    private String transactionId;

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getAuthCode() {
        return authCode;
    }
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
    public BigDecimal getAuthorizedAmount() {
        return authorizedAmount;
    }
    public void setAuthorizedAmount(BigDecimal authorizedAmount) {
        this.authorizedAmount = authorizedAmount;
    }
    public String getClientTransactionId() {
        return clientTransactionId;
    }
    public void setClientTransactionId(String clientTransactionId) {
        this.clientTransactionId = clientTransactionId;
    }
    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public String getIssuerResponseCode() {
        return issuerResponseCode;
    }
    public void setIssuerResponseCode(String issuerResponseCode) {
        this.issuerResponseCode = issuerResponseCode;
    }
    public String getIssuerResponseMessage() {
        return issuerResponseMessage;
    }
    public void setIssuerResponseMessage(String issuerResponseMessage) {
        this.issuerResponseMessage = issuerResponseMessage;
    }
    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }
    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }
    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }
    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }
    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }
    public String getReferenceNumber() {
        return referenceNumber;
    }
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }
    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public BigDecimal getConvenienceAmount() {
        return convenienceAmount;
    }
    public void setConvenienceAmount(BigDecimal convenienceAmount) {
        this.convenienceAmount = convenienceAmount;
    }
    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }
    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }
}
