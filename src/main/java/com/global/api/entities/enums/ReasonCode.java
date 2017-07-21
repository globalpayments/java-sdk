package com.global.api.entities.enums;

public enum ReasonCode implements IStringConstant {
    Fraud("FRAUD"),
    FalsePositive("FALSEPOSITIVE"),
    OutOfStock("OUTOFSTOCK"),
    InStock("INSTOCK"),
    Other("OTHER"),
    NotGiven("NOTGIVEN");

    String value;
    ReasonCode(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
