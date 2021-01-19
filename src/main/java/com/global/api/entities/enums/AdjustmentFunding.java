package com.global.api.entities.enums;

public enum AdjustmentFunding implements IStringConstant {
    Credit("CREDIT"),
    Debit("DEBIT");

    private final String value;
    AdjustmentFunding(String value) { this.value = value; }
    public byte[] getBytes() {
        return value.getBytes();
    }
    public String getValue() {
        return value;
    }
}