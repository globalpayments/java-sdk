package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum FeeType implements IStringConstant {
    TransactionFee("00"),
    Surcharge("22");

    private final String value;
    FeeType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
