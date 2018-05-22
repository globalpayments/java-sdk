package com.global.api.entities.enums;

public enum EmploymentStatus implements IStringConstant {
    Active("A"),
    Inactive("I"),
    Terminated("T");

    String value;
    EmploymentStatus(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
