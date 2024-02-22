package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum EmvPDLCardType implements IStringConstant {
    None("  "),
    Visa("01"),
    Mastercard("02"),
    AmericanExpress("03"),
    Discover("04"),
    Wex("05"),
    VOYAGER("06"),
    JCB("07"),
    UNIONPAY("08");

    final String value;

    EmvPDLCardType(String value){
        this.value = value;
    }
    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
