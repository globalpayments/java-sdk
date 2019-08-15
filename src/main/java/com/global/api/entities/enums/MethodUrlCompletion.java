package com.global.api.entities.enums;

public enum MethodUrlCompletion implements IStringConstant {
    Yes("YES"),
    No("NO"),
    Unavailable("UNAVAILABLE");

    String value;
    MethodUrlCompletion(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
