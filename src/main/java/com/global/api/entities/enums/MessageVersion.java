package com.global.api.entities.enums;

public enum MessageVersion implements IStringConstant {
    Version_210("2.1.0");

    String value;
    MessageVersion(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
