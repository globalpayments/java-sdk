package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum Channel implements IStringConstant {
    CardPresent("CP"),
    CardNotPresent("CNP");

    private String value;

    Channel(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

}