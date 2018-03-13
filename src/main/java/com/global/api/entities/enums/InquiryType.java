package com.global.api.entities.enums;

public enum InquiryType implements IStringConstant {
    Foodstamp("FOODSTAMP"),
    Cash("CASH");

    String value;
    InquiryType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
