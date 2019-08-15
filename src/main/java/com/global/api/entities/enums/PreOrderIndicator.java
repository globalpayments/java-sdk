package com.global.api.entities.enums;

public enum PreOrderIndicator implements IStringConstant {
    MerchandiseAvailable("MERCHANDISE_AVAILABLE"),
    FutureAvailability("FUTURE_AVAILABILITY");

    String value;
    PreOrderIndicator(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
