package com.global.api.terminals.pax.interfaces;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.utils.IOUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaxHttpInterface implements IDeviceCommInterface {
    private ITerminalConfiguration _settings;
    private HttpURLConnection _client;
    private IMessageSentInterface onMessageSent;

    public void setMessageSentHandler(IMessageSentInterface messageInterface) {
        this.onMessageSent = messageInterface;
    }
    
    public void setBroadcastMessageHandler(IBroadcastMessageInterface broadcastInterface) {
        // not required for this connection mode
	}

    public PaxHttpInterface(ITerminalConfiguration settings) {
        this._settings = settings;
    }

    public void connect() {
        // not required for this connection mode
    }

    public void disconnect() {
        // not required for this connection mode
    }

    public byte[] send(IDeviceMessage message) throws ApiException {
        if(onMessageSent != null)
            onMessageSent.messageSent(message.toString());

        String payload = Base64.encodeBase64String(message.getSendBuffer()).replace("\r", "").replace("\n", "");

        String endpoint = String.format("http://%s:%d?%s", _settings.getIpAddress(), _settings.getPort(), payload);
        try {
            _client = (HttpURLConnection) new URL(endpoint).openConnection();
        } catch(IOException e) {
            throw new ApiException(e.getMessage(), e);
        }

        try{
            _client.setDoInput(true);
            _client.setDoOutput(true);
            _client.setRequestMethod("GET");
            _client.addRequestProperty("Content-Type", "text/xml; charset=UTF-8");

            InputStream responseStream = _client.getInputStream();
            return IOUtils.readFully(responseStream).getBytes();
        } catch(IOException e){
            throw new MessageException("Failed to send message. Check inner exception for more details.", e);
        }
    }
}