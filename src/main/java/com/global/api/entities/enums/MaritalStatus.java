package com.global.api.entities.enums;

public enum MaritalStatus implements IStringConstant {
    Married("M"),
    Single("S");

    String value;
    MaritalStatus(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
