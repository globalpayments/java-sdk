package com.global.api.entities.enums;

public enum PayTypeCode implements IStringConstant {
    Hourly("H"),
    Salary("S"),
    T1099("T99"),
    T1099_Hourly("T99H"),
    Commission("C"),
    AutoHourly("Ah"),
    ManualSalary("Ms");

    String value;
    PayTypeCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
