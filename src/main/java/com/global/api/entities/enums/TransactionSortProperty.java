package com.global.api.entities.enums;

public enum TransactionSortProperty implements IStringConstant {
    Id("ID"),                   // Only available for Transactions report
    TimeCreated("TIME_CREATED"),// Available for both Transactions and Settled Transactions report
    Status("STATUS"),           // Only available for Settled Transactions report
    Type("TYPE"),               // Available for both Transactions and Settled Transactions report
    DepositId("DEPOSIT_ID");    // Only available for Settled Transactions report

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