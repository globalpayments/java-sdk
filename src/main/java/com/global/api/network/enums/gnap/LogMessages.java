package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum LogMessages implements IStringConstant {
    MessageType("Message Type"),
    MessageSubType("Message Sub Type"),
    TransactionCode("Transaction Code"),
    ProcessingFlag2("Processing Flag 2");

    String value;
    LogMessages(String value) {
        this.value=value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
