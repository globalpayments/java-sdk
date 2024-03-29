package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

// DE 63 Service Level
public enum ServiceLevel implements IStringConstant {
    FullServe("F"),
    MiniServe("N"),
    Other_NonFuel("O"),
    SelfServe("S"),
    MaxiServe("X"),
    NonFuelTransaction(" "),
    Other("2"),
    Unknown("9"),
    NoFuelPurchased("0");

    private final String value;
    ServiceLevel(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
