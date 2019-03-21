package com.global.api.gateways.events;

public class DisconnectEvent extends GatewayEvent {
    public String getEventMessage() {
        return super.getEventMessage().concat("Disconnected from host.");
    }

    public DisconnectEvent(String connectorName) {
        super(connectorName, GatewayEventType.Disconnected);
    }
}
