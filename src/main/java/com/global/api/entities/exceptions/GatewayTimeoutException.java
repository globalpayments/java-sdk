package com.global.api.entities.exceptions;

public class GatewayTimeoutException extends GatewayException {
    private String host;
    private String messageTypeIndicator;
    private String processingCode;
    private int reversalCount = 0;
    private String reversalResponseCode;
    private String reversalResponseText;
    private String transmissionTime;

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
    public String getProcessingCode() {
        return processingCode;
    }
    public void setProcessingCode(String processingCode) {
        this.processingCode = processingCode;
    }
    public int getReversalCount() {
        return reversalCount;
    }
    public void setReversalCount(int reversalCount) {
        this.reversalCount = reversalCount;
    }
    public String getReversalResponseCode() {
        return reversalResponseCode;
    }
    public void setReversalResponseCode(String reversalResponseCode) {
        this.reversalResponseCode = reversalResponseCode;
    }
    public String getReversalResponseText() {
        return reversalResponseText;
    }
    public void setReversalResponseText(String reversalResponseText) {
        this.reversalResponseText = reversalResponseText;
    }
    public String getTransmissionTime() {
        return transmissionTime;
    }
    public void setTransmissionTime(String transmissionTime) {
        this.transmissionTime = transmissionTime;
    }

    public GatewayTimeoutException() {
        super("The gateway did not respond within the given timeout.");
    }
    public GatewayTimeoutException(Exception innerException) {
        super("The gateway did not respond within the given timeout.", innerException);
    }
}
