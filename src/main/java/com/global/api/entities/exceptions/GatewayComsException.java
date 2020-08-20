package com.global.api.entities.exceptions;

public class GatewayComsException extends GatewayException {
    public GatewayComsException() {
        super("An error occurred while communicating with the gateway.");
    }
    public GatewayComsException(Exception innerException) {
        super("An error occurred while communicating with the gateway. Please see the inner exception for further details.", innerException);
    }
}
