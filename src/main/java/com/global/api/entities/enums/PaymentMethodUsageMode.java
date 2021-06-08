package com.global.api.entities.enums;

public enum PaymentMethodUsageMode implements IStringConstant {
    Single("SINGLE"),
    Multiple("MULTIPLE");

    String value;
    PaymentMethodUsageMode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
