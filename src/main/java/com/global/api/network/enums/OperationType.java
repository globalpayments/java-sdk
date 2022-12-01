package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum OperationType implements IStringConstant {
    Reserved("1"),
    Decrypt("2");

    String value;
    OperationType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
