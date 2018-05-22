package com.global.api.entities.enums;

public enum PayGroupFrequency implements INumericConstant {
    Annually(1),
    Quarterly(4),
    Monthly(12),
    SemiMonthly(24),
    BiWeekly(26),
    Weekly(52);

    int value;
    PayGroupFrequency(int value) {
        this.value = value;
    }
    public int getValue() { return value; }
}
