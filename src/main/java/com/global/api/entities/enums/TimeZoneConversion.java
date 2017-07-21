package com.global.api.entities.enums;

public enum TimeZoneConversion implements IStringConstant {
    UTC("UTC"),
    Merchant("Merchant"),
    Datacenter("Datacenter");

    String value;
    TimeZoneConversion(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
