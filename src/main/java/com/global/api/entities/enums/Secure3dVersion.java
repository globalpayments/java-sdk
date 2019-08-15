package com.global.api.entities.enums;

public enum Secure3dVersion implements IStringConstant {
    NONE("None"),
    ONE("One"),
    TWO("Two"),
    ANY("Any");

    String value;
    Secure3dVersion(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
