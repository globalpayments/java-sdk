package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum TransactionSecurityIndicator implements IStringConstant {
    NoSecurityConcern("0"),
    IdentificationVerified("1");
    String value;
    TransactionSecurityIndicator(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
