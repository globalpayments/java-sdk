package com.global.api.entities.exceptions;

public class UnsupportedPaymentMethodException extends ApiException {

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }

    public UnsupportedPaymentMethodException(String message, Exception innerException) {
        super(message, innerException);
    }
}
