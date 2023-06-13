package com.global.api.entities.enums;

public enum TermsVersion implements INumericConstant{
    merchant_US(1),
    payment_US(2),
    merchant_CA(3),
    merchant_UK(4),
    merchant_AU(5);

    int value;
    TermsVersion(int value) {
        this.value = value;
    }
    public int getValue() { return value; }
}
