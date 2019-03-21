package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE22_CardPresence implements IStringConstant {
    CardNotPresent("0"),
    CardPresent("1"),
    CardOnFile("8");

    private final String value;
    DE22_CardPresence(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
