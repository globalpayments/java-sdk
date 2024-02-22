package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum PurchaseRestrictionCapability implements IStringConstant {

    NOTAVISAFLEET2DOT0(" "),
    NOCHIIPANDHOSTBASEDPRODUCTRESTRICTION("0"),
    CHIPBASEDPRODUCTRESTRICTION("1"),
    HOSTBASEDPRODUCTRESTRICTION("2"),
    BOTHCHIPANDHOSTBASEDPRODUCTRESTRICTION("3");

    private final String value;
    PurchaseRestrictionCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
