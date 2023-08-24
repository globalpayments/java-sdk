package com.global.api.entities.exceptions;

public class GatewayException extends ApiException {
    private String responseCode;
    private String responseText;
    private String host;
    private String messageTypeIndicator;
    private String posDataCode;
    private String processingCode;
    private String transactionToken;
    private String transmissionTime;
    private int reversalCount = 0;
    private String reversalResponseCode;
    private String reversalResponseText;

    public String getResponseCode() {
        return responseCode;
    }
    public String getResponseText() {
        return responseText;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getMessageTypeIndicator() {
        return messageTypeIndicator;
    }
    public void setMessageTypeIndicator(String messageTypeIndicator) {
        this.messageTypeIndicator = messageTypeIndicator;
    }
    public String getPosDataCode() {
        return posDataCode;
    }
    public void setPosDataCode(String posDataCode) {
        this.posDataCode = posDataCode;
    }
    public String getProcessingCode() {
        return processingCode;
    }
    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }
    public String getTransactionToken() {
        return transactionToken;
    }
    public void setTransactionToken(String transactionToken) {
        this.transactionToken = transactionToken;
    }
    public String getTransmissionTime() {
        return transmissionTime;
    }
    public void setTransmissionTime(String transmissionTime) {
        this.transmissionTime = transmissionTime;
    }
    public int getReversalCount(){return reversalCount;}
    public void setReversalCount(int reversalCount) {
        this.reversalCount = reversalCount;
    }
    public String getReversalResponseCode(){return reversalResponseCode;}
    public void setReversalResponseCode(String reversalResponseCode) {
        this.reversalResponseCode = reversalResponseCode;
    }

    public void setReversalResponseText(String reversalResponseText) {
        this.reversalResponseText = reversalResponseText;
    }

    public GatewayException(String message) {
        this(message, null);
    }
    public GatewayException(String message, Exception innerException) {
        super(message, innerException);
    }
    public GatewayException(String message, Exception innerException, String responseCode, String responseText) {
        this(message, innerException);
        this.responseCode = responseCode;
        this.responseText = responseText;
    }
    public GatewayException(String message, String responseCode, String responseText) {
        super(message);
        this.responseCode = responseCode;
        this.responseText = responseText;
    }
}
