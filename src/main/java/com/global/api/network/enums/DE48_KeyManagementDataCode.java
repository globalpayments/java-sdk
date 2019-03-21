package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_KeyManagementDataCode implements IStringConstant {
    NoKey("0"),
    DerivedUniqueKeyPerTransaction_DUKPT("3"),
    ANSI("4"),
    PinPadCharacter("5");

    private final String value;
    DE48_KeyManagementDataCode(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
