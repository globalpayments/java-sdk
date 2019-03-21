package com.global.api.gateways.events;

public interface IGatewayEvent {
    GatewayEventType getEventType();
    String getTimestamp();
    String getEventMessage();
}
