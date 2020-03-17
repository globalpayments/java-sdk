package com.global.api.gateways.events;

public class SslHandshakeEvent extends GatewayEvent {
    private Exception sslException;

    public String getEventMessage() {
        String rvalue = super.getEventMessage();

        if(sslException != null) {
            rvalue = rvalue.concat(String.format("SSL Handshake Failed: %s", sslException.getMessage()));
        }
        else {
            rvalue = rvalue.concat("SSL Handshake Success");
        }

        return rvalue;
    }

    public SslHandshakeEvent(String connectorName, Exception exc) {
        super(connectorName, GatewayEventType.SslHandshake);
        sslException = exc;
    }
}
