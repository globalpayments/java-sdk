package com.global.api.entities.enums;

public enum EmploymentCategory implements IStringConstant {
    FullTime("FT"),
    PartTime("PT");

    String value;
    EmploymentCategory(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
