package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ProductDataFormat implements IStringConstant {
    GlobalPaymentsStandardFormat("0"),
    ANSI_X9_TG23_Format("1"),
    GlobalPayments_ProductCoupon_Format("2"),
    VISAFLEET2Dot0("4");

    private final String value;
    ProductDataFormat(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
