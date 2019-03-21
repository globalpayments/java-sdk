package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_HostConnected implements IStringConstant {
    Unavailable("0"),
    PrimaryHost("1"),
    SecondaryHost("2");

    private final String value;
    DE48_HostConnected(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
