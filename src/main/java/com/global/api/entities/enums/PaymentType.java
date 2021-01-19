package com.global.api.entities.enums;

public enum PaymentType implements IStringConstant {
    Refund("REFUND"),
    Sale("SALE");

    String value;
    PaymentType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
