package com.global.api.entities.enums;

public enum PaymentMethodFunction implements IStringConstant {
    PRIMARY_PAYOUT("PRIMARY_PAYOUT"),
    SECONDARY_PAYOUT("SECONDARY_PAYOUT"),
    ACCOUNT_ACTIVATION_FEE("ACCOUNT_ACTIVATION_FEE"),
    GROSS_BILLING("GROSS_BILLING"),
    FEES("FEES");

    String value;
    PaymentMethodFunction(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}