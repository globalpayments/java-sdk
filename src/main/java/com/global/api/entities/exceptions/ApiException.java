package com.global.api.entities.exceptions;

public class ApiException extends Exception {
    public ApiException(String message) {
        this(message, null);
    }
    public ApiException(String message, Exception innerException) {
        super(message, innerException);
    }
}
