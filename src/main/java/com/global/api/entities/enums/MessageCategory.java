package com.global.api.entities.enums;

public enum MessageCategory implements IStringConstant {
    PaymentAuthentication("PAYMENT_AUTHENTICATION"),
    NonPaymentAuthentication("NON_PAYMENT_AUTHENTICATION");

    String value;
    MessageCategory(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
