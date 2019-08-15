package com.global.api.entities.enums;

public enum StoredCredentialType implements IStringConstant {
    OneOff("oneoff"),
    Installment("installment"),
    Recurring("recurring");

    String value;
    StoredCredentialType(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
