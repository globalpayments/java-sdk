package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum Iso4217_CurrencyCode implements IStringConstant {
    CAD("124"),
    USD("840");

    private final String value;
    Iso4217_CurrencyCode(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
