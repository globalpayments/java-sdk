package com.global.api.terminals.upa.interfaces;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.terminals.DeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.utils.JsonDoc;

import java.nio.charset.StandardCharsets;

public class UpaMicInterface extends DeviceCommInterface {
    private final GpApiConfig gpApiConfig;
    private GpApiConnector connector;
    private String lastConnectionError;

    public UpaMicInterface(ITerminalConfiguration settings) {
        super(settings);
        gpApiConfig = (GpApiConfig)settings.getGatewayConfig();
    }

    public void connect() {
        try {
            connector = (GpApiConnector) ServicesContainer
                    .getInstance()
                    .getGateway("_upa_passthrough");
        } catch (ApiException e) {
            lastConnectionError = e.getMessage();
        }
    }

    public void disconnect() { /* NOM NOM */ }

    public byte[] send(IDeviceMessage message) throws ApiException {
        connect();

        if (lastConnectionError != null) {
            throw new MessageException(String.format("Could not connect to the device. %s", lastConnectionError));
        }

        JsonDoc requestBuilder = (JsonDoc) message.getRequestBuilder();
        String requestId = null;
        if (requestBuilder != null) {
            JsonDoc data = requestBuilder.get("data");
            if (data != null) {
                requestId = data.getStringOrNull("requestId");
            }
        }

        JsonDoc request = new JsonDoc();
        request.set("merchant_id", gpApiConfig.getMerchantId());
        request.set("account_id", gpApiConfig.getAccessTokenInfo() == null ? null :
                gpApiConfig.getAccessTokenInfo().getTransactionProcessingAccountID());
        request.set("account_name", gpApiConfig.getAccessTokenInfo() == null ? null :
                gpApiConfig.getAccessTokenInfo().getTransactionProcessingAccountName());
        request.set("channel", gpApiConfig.getChannel());
        request.set("country", gpApiConfig.getCountry());
        request.set("currency", gpApiConfig.getDeviceCurrency());
        request.set("reference", requestId);
        request.set("request", requestBuilder);

        JsonDoc notifications = request.subElement("notifications");
        notifications.set("status_url", gpApiConfig.getMethodNotificationUrl());

        JsonDoc requestJsonDoc = new JsonDoc()
                .set("verb", GpApiRequest.HttpMethod.Post.getValue())
                .set("url", GpApiRequest.DEVICE_ENDPOINT)
                .set("content_length", request.toString().length())
                .set("content", request);
        raiseOnMessageSent(requestJsonDoc.toString());

        String response = connector.processPassThrough(request);
        raiseOnMessageReceived(response.getBytes(StandardCharsets.UTF_8));

        return response.getBytes(StandardCharsets.UTF_8);
    }
}
