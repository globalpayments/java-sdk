package com.global.api.entities.enums;

import com.global.api.entities.enums.IStringConstant;

public enum Language implements IStringConstant {
    English("EN"),
    Spanish("ES");

    String value;
    Language(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
