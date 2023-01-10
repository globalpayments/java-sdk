package com.global.api.entities.transactionApi.entities;

import com.global.api.entities.enums.IStringConstant;

public enum TransactionApiRegion implements IStringConstant {
    US("US"),
    CA("CA");

    private final String value;

    TransactionApiRegion(String value){
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}
