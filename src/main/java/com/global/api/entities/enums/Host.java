package com.global.api.entities.enums;

public enum Host implements IStringConstant {
    Primary("primary"),
    Secondary("secondary");

    String value;
    Host(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
