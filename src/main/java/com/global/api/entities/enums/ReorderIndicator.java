package com.global.api.entities.enums;

public enum ReorderIndicator implements IStringConstant {
    FirstTimeOrder("FIRST_TIME_ORDER"),
    Reorder("REORDER");

    String value;
    ReorderIndicator(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
