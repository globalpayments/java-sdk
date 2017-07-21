package com.global.api.entities.enums;

public enum ScheduleFrequency implements IStringConstant {
    Weekly("Weekly"),
    BiWeekly("Bi-Weekly"),
    BiMonthly("Bi-Monthly"),
    SemiMonthly("Semi-Monthly"),
    Monthly("Monthly"),
    Quarterly("Quarterly"),
    SemiAnnually("Semi-Annually"),
    Annually("Annually");

    String value;
    ScheduleFrequency(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
