package com.global.api.gateways.events;

public class TimeoutEvent extends GatewayEvent {
    public TimeoutEvent(String connectorName, GatewayEventType eventType) {
        super(connectorName, eventType);
    }

    public String getEventMessage() {
        String rvalue = super.getEventMessage();
        if(getEventType().equals(GatewayEventType.Timeout)) {
            rvalue = rvalue.concat("Host did not respond on primary or secondary hosts.");
        }
        else {
            rvalue = rvalue.concat("Host did not respond on primary host, connecting to secondary host.");
        }

        return rvalue;
    }
}
