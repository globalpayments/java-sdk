package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;

public class DE48_39_PriorMessageInformation implements IDataElement<DE48_39_PriorMessageInformation> {
    private String responseTime = "999";
    private String connectTime = "999";
    private String cardType = "    ";
    private String messageTransactionIndicator = "0000";
    private String processingCode = "000000";
    private String stan = "000000";

    public String getResponseTime() {
        return responseTime;
    }
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
    public String getConnectTime() {
        return connectTime;
    }
    public void setConnectTime(String connectTime) {
        this.connectTime = connectTime;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
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
    public String getStan() {
        return stan;
    }
    public void setStan(String stan) {
        this.stan = stan;
    }

    public DE48_39_PriorMessageInformation fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        responseTime = sp.readString(3);
        connectTime = sp.readString(3);
        cardType = sp.readString(4);
        messageTransactionIndicator = sp.readString(4);
        processingCode = sp.readString(6);
        stan = sp.readString(6);

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = responseTime
                .concat(connectTime)
                .concat(cardType)
                .concat(messageTransactionIndicator)
                .concat(processingCode)
                .concat(stan);

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
