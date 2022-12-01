package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ServiceType implements IStringConstant {
    GPN_API("G");

    String value;
    ServiceType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
