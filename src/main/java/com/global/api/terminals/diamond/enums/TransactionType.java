package com.global.api.terminals.diamond.enums;

public enum TransactionType {
    UNKNOWN("0"),
    SALE("1"),
    PREAUTH("4"),
    CAPTURE("5"),
    REFUND("6"),
    VOID("10"),
    REPORT("66"),
    PREAUTH_CANCEL("82"),
    INCR_AUTH("86");

    private String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromValue(String value) {
        for (TransactionType transactionType : TransactionType.values()) {
            if (transactionType.getValue().equals(value)) {
                return transactionType;
            }
        }
        return null;
    }
}
