package com.global.api.entities.enums;

public enum BillPresentment implements IStringConstant {
    FULL("FULL");

    String value;
    BillPresentment(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
