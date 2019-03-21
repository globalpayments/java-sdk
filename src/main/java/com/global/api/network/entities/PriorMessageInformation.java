package com.global.api.network.entities;

public class PriorMessageInformation {
    private String responseTime = "999";
    private String cardType = "    ";
    private String functionCode = "000";
    private String processingCode = "000000";
    private String messageReasonCode;
    private String messageTransactionIndicator = "0000";
    private String systemTraceAuditNumber = "000000";

    public String getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
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
    public String getMessageTransactionIndicator() {
        return messageTransactionIndicator;
    }
    public void setMessageTransactionIndicator(String messageTransactionIndicator) {
        this.messageTransactionIndicator = messageTransactionIndicator;
    }
    public String getProcessingCode() {
        return processingCode;
    }
    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }
    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }
    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }
}
