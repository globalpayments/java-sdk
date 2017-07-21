package com.global.api.entities.exceptions;

public class GatewayException extends ApiException {
    private String responseCode;
    private String responseText;

    public String getResponseCode() {
        return responseCode;
    }
    public String getResponseText() {
        return responseText;
    }

    public GatewayException(String message) {
        this(message, null);
    }
    public GatewayException(String message, Exception innerException) {
        super(message, innerException);
    }
    public GatewayException(String message, String responseCode, String responseText) {
        super(message);
        this.responseCode = responseCode;
        this.responseText = responseText;
    }
}
