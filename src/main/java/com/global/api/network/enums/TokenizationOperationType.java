package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum TokenizationOperationType implements IStringConstant {

    Tokenize("1"),
    DeTokenize("2"),
    DeleteToken("3"),
    UpdateToken("4");

    String value;
    TokenizationOperationType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
