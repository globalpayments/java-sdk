package com.global.api.entities.exceptions;

import com.global.api.gateways.events.IGatewayEvent;

import java.util.LinkedList;

public class GatewayException extends ApiException {
    private String responseCode;
    private String responseText;
    private LinkedList<IGatewayEvent> gatewayEvents;

    public String getResponseCode() {
        return responseCode;
    }
    public String getResponseText() {
        return responseText;
    }
    public LinkedList<IGatewayEvent> getGatewayEvents() {
        return gatewayEvents;
    }
    public void setGatewayEvents(LinkedList<IGatewayEvent> gatewayEvents) {
        this.gatewayEvents = gatewayEvents;
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
