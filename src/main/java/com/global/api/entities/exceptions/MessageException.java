package com.global.api.entities.exceptions;

public class MessageException extends ApiException {
    public MessageException(String message) {
        super(message);
    }
    public MessageException(String message, Exception innerException) {
        super(message, innerException);
    }
}
