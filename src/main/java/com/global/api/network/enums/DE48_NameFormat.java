package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_NameFormat implements IStringConstant {
    FreeFormat("0"),
    Delimited_FirstMiddleLast("1"),
    Delimited_Title("2");

    private final String value;
    DE48_NameFormat(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
