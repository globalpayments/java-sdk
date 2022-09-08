package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum BatchTotalSign implements IStringConstant {
    Positive("+"),
    Negative("-"),
    Default("0");

    String value;
    BatchTotalSign(String value) {
        this.value=value;
    }
    public String getValue() { return this.value;}
    public byte[] getBytes() { return this.value.getBytes(); }
}
