package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardType implements IStringConstant {
    Credit("C"),
    Debit("D");

    String value;
    CardType(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
