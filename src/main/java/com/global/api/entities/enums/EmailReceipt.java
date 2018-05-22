package com.global.api.entities.enums;

public enum EmailReceipt implements IStringConstant {
    Never("Never"),
    All("All"),
    Approvals("Approvals"),
    Declines("Declines");

    String value;
    EmailReceipt(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
