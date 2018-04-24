package com.global.api.entities.enums;

public enum FilterPayTypeCode implements IStringConstant {
    Hourly("H"),
    T1099("1099");

    String value;
    FilterPayTypeCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
