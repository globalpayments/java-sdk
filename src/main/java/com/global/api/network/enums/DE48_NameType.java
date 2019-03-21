package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_NameType implements IStringConstant {
    CardHolderName("0"),
    CompanyName("1"),
    Secondary_Joint_AccountName("2");

    private final String value;
    DE48_NameType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
