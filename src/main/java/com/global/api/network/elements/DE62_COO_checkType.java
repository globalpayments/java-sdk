package com.global.api.network.elements;

import com.global.api.entities.enums.IStringConstant;

import java.nio.charset.StandardCharsets;

public enum DE62_COO_checkType implements IStringConstant {
    PERSONAL("0"),
    PAYROLL("1"),
    GOVERNMENT("2"),
    BUSINESS("3");
    private String value;
    DE62_COO_checkType(String value) {this.value = value; }

    public byte[] getBytes() {return value.getBytes(StandardCharsets.UTF_8);}

    public String getValue() {return value;}
}
