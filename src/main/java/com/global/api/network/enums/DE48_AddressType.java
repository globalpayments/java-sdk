package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_AddressType implements IStringConstant {
    StreetAddress("0"),
    AddressVerification("1"),
    PhoneNumber("2"),
    Email("3"),
    PrefixedUrl("4"),
    AddressVerification_Numeric("5");

    private final String value;
    DE48_AddressType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
