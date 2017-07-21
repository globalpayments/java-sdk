package com.global.api.entities.enums;

public enum RecurringSequence implements IStringConstant {
    First("First"),
    Subsequent("Subsequent"),
    Last("Last");

    String value;
    RecurringSequence(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
