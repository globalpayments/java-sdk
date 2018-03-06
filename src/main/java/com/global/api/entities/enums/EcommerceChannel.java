package com.global.api.entities.enums;

public enum EcommerceChannel implements IStringConstant {
    Ecom("ECOM"),
    Moto("MOTO");

    String value;
    EcommerceChannel(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
