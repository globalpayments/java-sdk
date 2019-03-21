package com.global.api.gateways.events;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class GatewayEvent implements IGatewayEvent {
    private String connectorName;
    private GatewayEventType eventType;
    protected DateTime timestamp;

    GatewayEvent(String connectorName, GatewayEventType eventType) {
        this.connectorName = connectorName;
        this.eventType = eventType;
        this.timestamp = DateTime.now(DateTimeZone.UTC);
    }

    public String getConnectorName() {
        return connectorName;
    }
    public GatewayEventType getEventType() {
        return eventType;
    }
    public String getTimestamp() {
        return timestamp.toString("MM-dd-yyyy hh:mm:ss.SSS");
    }
    public String getEventMessage() {
        return String.format("[%s] - ", getTimestamp());
    }
}
