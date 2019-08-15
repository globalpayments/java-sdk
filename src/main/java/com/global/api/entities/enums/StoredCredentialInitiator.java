package com.global.api.entities.enums;

public enum StoredCredentialInitiator implements IStringConstant {
    CardHolder("cardholder"),
    Merchant("merchant"),
    Scheduled("scheduled");

    String value;
    StoredCredentialInitiator(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
