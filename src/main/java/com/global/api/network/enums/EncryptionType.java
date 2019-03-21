package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum EncryptionType implements IStringConstant {
    TEP1("1"),
    TEP2("2");

    String value;
    EncryptionType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
