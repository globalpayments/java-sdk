package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ProductDataFormat implements IStringConstant {
    HeartlandStandardFormat("0"),
    ANSI_X9_TG23_Format("1"),
    Heartland_ProductCoupon_Format("2");

    private final String value;
    ProductDataFormat(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
