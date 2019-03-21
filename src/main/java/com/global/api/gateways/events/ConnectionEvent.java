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
    private boolean sslNavigation;

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
    public void setSslNavigation(boolean sslNavigation) {
        this.sslNavigation = sslNavigation;
    }

    private long getConnectionTime() {
        DateTime d1 = connectionCompleted != null ? connectionCompleted : connectionFailOver;

        if (d1 != null) {
            return d1.getMillis() - connectionStarted.getMillis();
        }
        else {
            return 0;
        }
    }
    public String getEventMessage() {
        String rvalue = super.getEventMessage();
        return rvalue.concat(String.format("Connecting to %s host (%s:%s); started: %s; ssl success: %s; completed: %s; fail over: %s; attempts: %s; connection time (milliseconds): %sms.",
                host,
                endpoint,
                port,
                connectionStarted.toString("hh:mm:ss.SSS"),
                sslNavigation,
                connectionCompleted != null ? connectionCompleted.toString("hh:mm:ss.SSS") : "null",
                connectionFailOver != null ? connectionFailOver.toString("hh:mm:ss.SSS") : "null",
                connectionAttempts + 1,
                getConnectionTime()
        ));
    }

    public ConnectionEvent(String connectorName) {
        super(connectorName, GatewayEventType.Connection);
    }
}
