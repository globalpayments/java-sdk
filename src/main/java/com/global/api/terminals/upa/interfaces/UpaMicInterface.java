package com.global.api.terminals.upa.interfaces;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.gateways.GpApiConnector;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.abstractions.IUPAMessage;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.JsonDoc;

import java.nio.charset.StandardCharsets;

import static com.global.api.logging.PrettyLogger.generateRequestLog;

public class UpaMicInterface implements IDeviceCommInterface, IUPAMessage {

    private final ITerminalConfiguration config;
    private final GpApiConfig gpApiConfig;
    private GpApiConnector _connector;
    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;
    private String lastConnectionError;

    public UpaMicInterface(ITerminalConfiguration config) {
        this.config = config;
        gpApiConfig = (GpApiConfig) config.getGatewayConfig();
    }

    public void connect() {
        try {
            _connector = (GpApiConnector) ServicesContainer.getInstance().getGateway("_upa_passthrough");
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
        try {
            config.getLogManagementProvider().RequestSent(generateRequestLog(requestJsonDoc));
        } catch (Exception exc) {
            throw new GatewayException("Error occurred while sending the request.", exc);
        }

        String response = _connector.processPassThrough(request);

        return response.getBytes(StandardCharsets.UTF_8); // TODO is it good enough to get the bytes?
    }

    @Override
    public void setMessageSentHandler(IMessageSentInterface messageInterface) {
        this.onMessageSent = messageInterface;
    }

    @Override
    public void setMessageReceivedHandler(IMessageReceivedInterface onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }
}
