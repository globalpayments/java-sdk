package com.global.api.entities.enums;

public enum AccountType implements IStringConstant {
    Checking("CHECKING"),
    Savings("SAVINGS");

    String value;
    AccountType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
