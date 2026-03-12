package com.global.api.entities.enums;

public enum DataResidency {
    NONE("NONE"),
    EU("EU");

    private String value;

    DataResidency(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}

