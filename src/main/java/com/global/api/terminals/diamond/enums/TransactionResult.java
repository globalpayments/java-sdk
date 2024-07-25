package com.global.api.terminals.diamond.enums;

public enum TransactionResult {
    ACCEPTED("0"),
    REFUSED("1"),
    NO_CONNECTION("2"),
    CANCELED("7");

    private String value;

    TransactionResult(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionResult fromValue(String value) {
        for (TransactionResult transactionResult : TransactionResult.values()) {
            if (transactionResult.getValue().equals(value)) {
                return transactionResult;
            }
        }
        return null;
    }
}
