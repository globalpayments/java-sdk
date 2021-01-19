package com.global.api.entities.enums;

public enum TransactionSortProperty implements IStringConstant {
    DepositId("DEPOSIT_ID"),
    TimeCreated("TIME_CREATED"),
    Type("TYPE"),
    Status("STATUS");

    private String value;

    TransactionSortProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }

}