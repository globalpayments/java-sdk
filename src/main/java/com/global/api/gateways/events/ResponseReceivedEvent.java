package com.global.api.gateways.events;

import org.joda.time.DateTime;

public class ResponseReceivedEvent extends GatewayEvent {
    private DateTime requestSent;

    public String getEventMessage() {
        String rvalue = super.getEventMessage();
        return rvalue.concat(String.format("Host response received. Response time (milliseconds): %sms.", getResponseTime()));
    }
    private long getResponseTime() {
        if(requestSent != null) {
            return timestamp.getMillis() - requestSent.getMillis();
        }
        else return 0;
    }

    public ResponseReceivedEvent(String connectorName, DateTime requestSent) {
        super(connectorName, GatewayEventType.ResponseReceived);
        this.requestSent = requestSent;
    }
}
