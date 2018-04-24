package com.global.api.entities.enums;

public enum Gender implements IStringConstant {
    Female("F"),
    Male("M");

    String value;
    Gender(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
