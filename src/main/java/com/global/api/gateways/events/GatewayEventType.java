package com.global.api.gateways.events;

public enum GatewayEventType {
    Connection,
    Disconnected,
    RequestSent,
    ResponseReceived,
    Timeout,
    TimeoutFailOver
}
