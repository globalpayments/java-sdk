package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum NetworkTransactionType implements IStringConstant {
    KeepAlive("NT"),
    Transaction("EH");

    private final String value;
    NetworkTransactionType(String value) { this.value = value; }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

    public String getValue() {
        return this.value;
    }
}
