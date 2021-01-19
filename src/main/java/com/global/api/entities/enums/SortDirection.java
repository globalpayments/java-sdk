package com.global.api.entities.enums;

public enum SortDirection implements IStringConstant {
    Ascending("ASC"),
    Descending("DESC");

    String value;

    SortDirection(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

}