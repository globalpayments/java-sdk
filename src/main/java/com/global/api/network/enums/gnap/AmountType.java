package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum AmountType implements IStringConstant {
    OriginalAmount("57"),
    LedgerBalance("01"),
    AvailableBalance("02");

    String value;
    AmountType(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
