package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.Transaction;
import sun.misc.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;

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
