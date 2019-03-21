package com.global.api.gateways.events;

public class RequestSentEvent extends GatewayEvent {
    public String getEventMessage() {
        return super.getEventMessage().concat("Request sent to host.");
    }

    public RequestSentEvent(String connectorName) {
        super(connectorName, GatewayEventType.RequestSent);
    }
}
