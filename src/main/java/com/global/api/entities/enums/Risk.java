package com.global.api.entities.enums;

public enum Risk implements IStringConstant {
    HIGH("High"),
    LOW("Low");

    String value;
    Risk(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}