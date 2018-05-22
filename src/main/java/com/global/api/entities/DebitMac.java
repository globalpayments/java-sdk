package com.global.api.entities;

public class DebitMac {
    private String transactionCode;
    private String transmissionNumber;
    private String bankResponseCode;
    private String macKey;
    private String pinKey;
    private String fieldKey;
    private String traceNumber;
    private String messageAuthenticationCode;

    public String getTransactionCode() {
        return transactionCode;
    }
    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }
    public String getTransmissionNumber() {
        return transmissionNumber;
    }
    public void setTransmissionNumber(String transmissionNumber) {
        this.transmissionNumber = transmissionNumber;
    }
    public String getBankResponseCode() {
        return bankResponseCode;
    }
    public void setBankResponseCode(String bankResponseCode) {
        this.bankResponseCode = bankResponseCode;
    }
    public String getMacKey() {
        return macKey;
    }
    public void setMacKey(String macKey) {
        this.macKey = macKey;
    }
    public String getPinKey() {
        return pinKey;
    }
    public void setPinKey(String pinKey) {
        this.pinKey = pinKey;
    }
    public String getFieldKey() {
        return fieldKey;
    }
    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }
    public String getTraceNumber() {
        return traceNumber;
    }
    public void setTraceNumber(String traceNumber) {
        this.traceNumber = traceNumber;
    }
    public String getMessageAuthenticationCode() {
        return messageAuthenticationCode;
    }
    public void setMessageAuthenticationCode(String messageAuthenticationCode) {
        this.messageAuthenticationCode = messageAuthenticationCode;
    }
}
