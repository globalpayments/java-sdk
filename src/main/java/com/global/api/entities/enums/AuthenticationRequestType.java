package com.global.api.entities.enums;

public enum AuthenticationRequestType implements IStringConstant {
    PaymentTransaction("PAYMENT_TRANSACTION"),
    RecurringTransaction("RECURRING_TRANSACTION"),
    InstallmentTransaction("INSTALLMENT_TRANSACTION"),
    AddCard("ADD_CARD"),
    MaintainCard("MAINTAIN_CARD"),
    CardHolderVerification("CARDHOLDER_VERIFICATION");

    String value;
    AuthenticationRequestType(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
