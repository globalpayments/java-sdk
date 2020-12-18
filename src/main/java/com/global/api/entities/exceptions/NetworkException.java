package com.global.api.entities.exceptions;

public class NetworkException extends GatewayException {
    public NetworkException(String message) {
        this(message, null);
    }
    public NetworkException(String message, Exception innerException) {
        super(message, innerException);
    }
}
