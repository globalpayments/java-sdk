package com.global.api.entities.exceptions;

public class GatewayTimeoutException extends GatewayException {
    public GatewayTimeoutException() {
        super("The gateway did not respond within the given timeout.");
    }
    public GatewayTimeoutException(Exception innerException) {
        super("The gateway did not respond within the given timeout.", innerException);
    }
    public GatewayTimeoutException(String message,String gatewayRspCode, String gatewayRspText, String gatewayTxnId) {
        super(message, gatewayRspCode, gatewayRspText, gatewayTxnId);
    }
}