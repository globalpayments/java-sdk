package com.global.api.terminals.upa.Entities.Enums;

import com.global.api.entities.enums.IStringConstant;

public enum UpaSafType implements IStringConstant {
    APPROVED("AUTHORIZED TRANSACTIONS"),
    PENDING("PENDING TRANSACTIONS"),
    FAILED("FAILED TRANSACTIONS");

    String value;
    UpaSafType(String value){
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
