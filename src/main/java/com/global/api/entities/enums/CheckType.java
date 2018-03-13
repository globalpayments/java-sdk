package com.global.api.entities.enums;

public enum CheckType implements IStringConstant {
    Personal("PERSONAL"),
    Business("BUSINESS"),
    Payroll("PAYROLL");

    String value;
    CheckType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
