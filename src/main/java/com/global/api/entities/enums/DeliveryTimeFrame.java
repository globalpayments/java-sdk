package com.global.api.entities.enums;

public enum DeliveryTimeFrame implements IStringConstant {
    ElectronicDelivery("ELECTRONIC_DELIVERY"),
    SameDay("SAME_DAY"),
    Overnight("OVERNIGHT"),
    TwoOrMoreDays("TWO_DAYS_OR_MORE");

    String value;
    DeliveryTimeFrame(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
