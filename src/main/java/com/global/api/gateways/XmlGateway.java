package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;

public abstract class XmlGateway extends Gateway {
    public XmlGateway() {
        super("text/xml");
    }

    public String doTransaction(String request) throws GatewayException {
        GatewayResponse response = sendRequest("POST", "", request);
        if(response.getStatusCode() != 200)
            throw new GatewayException("Unexpected http status code [" + response.getStatusCode() + "]");
        return response.getRawResponse();
    }
}
