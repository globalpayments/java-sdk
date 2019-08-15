package com.global.api.entities.enums;

public enum SdkInterface implements IStringConstant {
    Native("NATIVE"),
    Browser("BROWSER"),
    Both("BOTH");

    String value;
    SdkInterface(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
