package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ProductCodeSet implements IStringConstant {
    GlobalPayments("0"),
    ClientSpecific("1"),
    IssuerSpecific("2"),
    Conexxus_3_Digit("3"),
    Conexxus_6_Digit("4"),
    UCC_12("5"),
    GTIN("6"),
    Mixed("7"),
    ClientSpecificAddendum_1("8"),
    ClientSpecificAddendum_2("9");

    private final String value;
    ProductCodeSet(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
