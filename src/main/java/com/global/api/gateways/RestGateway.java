package com.global.api.gateways;

import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.JsonDoc;

import java.util.HashMap;

public class RestGateway extends Gateway {
    public RestGateway() {
        super("application/json");
    }

    public String doTransaction(String verb, String endpoint) throws GatewayException {
        return doTransaction(verb, endpoint, null, null);
    }
    public String doTransaction(String verb, String endpoint, String data) throws GatewayException {
        return doTransaction(verb, endpoint, data, null);
    }
    public String doTransaction(String verb, String endpoint, String data, HashMap<String, String> queryStringParams) throws GatewayException {
        GatewayResponse response = sendRequest(verb, endpoint, data, queryStringParams);
        return handleResponse(response);
    }
    protected String handleResponse(GatewayResponse response) throws GatewayException {
        if(response.getStatusCode() != 200 && response.getStatusCode() != 204) {
            JsonDoc parsed = JsonDoc.parse(response.getRawResponse());
            if(parsed.has("error")) {
                JsonDoc error = parsed.get("error");
                throw new GatewayException(String.format("Status code: %s - %s", response.getStatusCode(), error.getString("message")));
            }
            throw new GatewayException(String.format("Status code: %s - %s", response.getStatusCode(), parsed.getString("message")));
        }
        return response.getRawResponse();
    }
}
