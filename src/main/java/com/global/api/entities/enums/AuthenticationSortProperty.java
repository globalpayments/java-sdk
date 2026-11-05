package com.global.api.entities.enums;

public enum AuthenticationSortProperty implements IStringConstant {
    Id("ID"),
    TimeCreated("TIME_CREATED"),
    Status("STATUS");

    private final String value;

    AuthenticationSortProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}
