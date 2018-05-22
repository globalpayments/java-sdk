package com.global.api.entities.exceptions;

public class UnsupportedTransactionException extends ApiException {
    public UnsupportedTransactionException() {
        this("Transaction type not supported for this payment method.");
    }
    public UnsupportedTransactionException(String message) {
        super(message);
    }
}
