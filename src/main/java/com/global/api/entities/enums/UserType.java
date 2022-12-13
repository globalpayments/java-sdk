package com.global.api.entities.enums;

public enum UserType implements IStringConstant {
    MERCHANT("Merchant");

    String value;
    UserType(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}