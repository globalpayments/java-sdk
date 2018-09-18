package com.global.api.entities.enums;

public enum AdvancedDepositType implements IStringConstant {
    AssuredReservation("ASSURED_RESERVATION"),
    CardDeposit("CARD_DEPOSIT"),
    Purchase("PURCHASE"),
    Other("OTHER");

    String value;
    AdvancedDepositType(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
