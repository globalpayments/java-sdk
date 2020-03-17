package com.global.api.gateways.events;

import org.joda.time.DateTime;

public class FailOverEvent extends GatewayEvent {
    private DateTime started;
    private DateTime complete;

    private long getConnectionTime() {
        if (complete != null) {
            return complete.getMillis() - started.getMillis();
        }
        else {
            return 0;
        }
    }
    public String getEventMessage() {
        String rvalue = super.getEventMessage();
        return rvalue.concat(String.format("Connection failed (milliseconds): %sms", getConnectionTime()));
    }

    public FailOverEvent(String connectorName, DateTime started, DateTime complete) {
        super(connectorName, GatewayEventType.FailOver);
        this.started = started;
        this.complete = complete;
    }
}
