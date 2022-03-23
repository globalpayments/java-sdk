package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum PDLTableID implements IStringConstant {
    Table10("10"),
    Table30("30"),
    Table40("40"),
    Table50("50"),
    Table60("60"),
    Table70("70"),
    Table80("80");
    private final String value;
    PDLTableID(String value) { this.value = value; }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
