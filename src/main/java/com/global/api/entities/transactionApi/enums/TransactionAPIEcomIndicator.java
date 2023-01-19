package com.global.api.entities.transactionApi.enums;

import com.global.api.entities.enums.IStringConstant;

public enum TransactionAPIEcomIndicator implements IStringConstant {
    ECOM_INDICATOR_1("1"),
    ECOM_INDICATOR_2("2"),
    ECOM_INDICATOR_3("3"),
    ECOM_INDICATOR_5("5"),
    ECOM_INDICATOR_7("7");

    final String value;

    TransactionAPIEcomIndicator(String value){
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
