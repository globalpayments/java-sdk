package com.global.api.gateways.events;

import org.joda.time.DateTime;

public class ConnectionEvent extends GatewayEvent {
    private String endpoint;
    private String port;
    private String host;
    private DateTime connectionStarted;
    private DateTime connectionCompleted;
    private int connectionAttempts;
    private DateTime connectionFailOver;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public void setConnectionStarted(DateTime connectionStarted) {
        this.connectionStarted = connectionStarted;
    }
    public void setConnectionCompleted(DateTime connectionCompleted) {
        this.connectionCompleted = connectionCompleted;
    }
    public void setConnectionAttempts(int connectionAttempts) {
        this.connectionAttempts = connectionAttempts;
    }
    public void setConnectionFailOver(DateTime connectionFailover) {
        this.connectionFailOver = connectionFailover;
    }

    public String getEventMessage() {
        String rvalue = super.getEventMessage();
        return rvalue.concat(String.format("Connecting to %s host (%s:%s): %s",
                host,
                endpoint,
                port,
                connectionStarted.toString("hh:mm:ss.SSS")
        ));
    }

    public ConnectionEvent(String connectorName) {
        super(connectorName, GatewayEventType.Connection);
    }
}
