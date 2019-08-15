package com.global.api.entities.enums;

public enum StoredCredentialSequence implements IStringConstant {
    First("first"),
    Subsequent("subsequent");

    String value;
    StoredCredentialSequence(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
