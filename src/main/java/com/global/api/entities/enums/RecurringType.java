package com.global.api.entities.enums;

public enum RecurringType implements IStringConstant {
    Fixed("Fixed"),
    Variable("Variable");

    String value;
    RecurringType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
