package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum IntervalToExpire implements IStringConstant {
    WEEK("WEEK"),
    DAY("DAY"),
    TWELVE_HOURS("12_HOURS"),
    SIX_HOURS("6_HOURS"),
    THREE_HOURS("3_HOURS"),
    ONE_HOUR("1_HOUR"),
    THIRTY_MINUTES("30_MINUTES"),
    TEN_MINUTES("10_MINUTES"),
    FIVE_MINUTES("5_MINUTES");

    String value;
    IntervalToExpire(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
