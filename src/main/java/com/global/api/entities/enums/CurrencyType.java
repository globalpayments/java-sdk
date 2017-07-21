package com.global.api.entities.enums;

public enum CurrencyType implements IStringConstant {
    Currency("USD"),
    Points("POINTS");

    String value;
    CurrencyType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
