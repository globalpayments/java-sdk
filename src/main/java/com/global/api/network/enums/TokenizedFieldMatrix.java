package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum TokenizedFieldMatrix implements IStringConstant {

    TokenizedData("2"),
    AccountNumber("1");

    String value;
    TokenizedFieldMatrix(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
