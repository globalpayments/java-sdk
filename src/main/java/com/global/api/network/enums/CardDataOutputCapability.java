package com.global.api.network.enums;


import com.global.api.entities.enums.IStringConstant;

public enum CardDataOutputCapability implements IStringConstant {
    Unknown("0"),
    None("1"),
    MagStripe_Write("2"),
    ICC("3");

    private final String value;
    CardDataOutputCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
