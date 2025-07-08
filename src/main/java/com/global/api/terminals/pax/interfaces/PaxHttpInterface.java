package com.global.api.terminals.pax.interfaces;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.utils.IOUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaxHttpInterface extends DeviceCommInterface {
    public PaxHttpInterface(ITerminalConfiguration settings) {
        super(settings);
    }

    public void connect() {
        // not required for this connection mode
    }

    public void disconnect() {
        // not required for this connection mode
    }

    public byte[] send(IDeviceMessage message) throws ApiException {
        raiseOnMessageSent(message.toString());

        String payload = Base64.encodeBase64String(message.getSendBuffer()).replace("\r", "").replace("\n", "");

        String endpoint = String.format("http://%s:%d?%s", settings.getIpAddress(), settings.getPort(), payload);

        HttpURLConnection client;
        try {
            client = (HttpURLConnection) new URL(endpoint).openConnection();
        } catch(IOException e) {
            throw new ApiException(e.getMessage(), e);
        }

        try{
            client.setDoInput(true);
            client.setDoOutput(true);
            client.setRequestMethod("GET");
            client.addRequestProperty("Content-Type", "text/xml; charset=UTF-8");

            InputStream responseStream = client.getInputStream();

            byte[] response = IOUtils.readFully(responseStream).getBytes();
            raiseOnMessageReceived(response);
            return response;
        } catch(IOException e){
            throw new MessageException("Failed to send message. Check inner exception for more details.", e);
        }
    }
}