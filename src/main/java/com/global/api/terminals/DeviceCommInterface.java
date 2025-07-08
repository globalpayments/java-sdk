package com.global.api.terminals;

import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.utils.JsonDoc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

public abstract class DeviceCommInterface implements IDeviceCommInterface {
    protected final ITerminalConfiguration settings;
    protected IMessageSentInterface onMessageSent;
    protected IMessageReceivedInterface onMessageReceived;

    @Override
    public void setMessageSentHandler(IMessageSentInterface messageInterface) {
        onMessageSent = messageInterface;
    }

    @Override
    public void setMessageReceivedHandler(IMessageReceivedInterface messageInterface) {
        onMessageReceived = messageInterface;
    }

    public DeviceCommInterface(ITerminalConfiguration settings) {
        this.settings = settings;
    }

    protected void raiseOnMessageSent(String message) {
        try {
            if (onMessageSent != null) {
                onMessageSent.messageSent(message);
            }

            if (settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "REQUEST");
                msg.set("message", message);

                settings.getRequestLogger().RequestSent(msg.toString());
            }
        }
        catch(IOException exc) {
            /* Logging should never interfere with processing */
        }
    }

    protected void raiseOnMessageReceived(byte[] message) {
        try {
            DeviceMessage parsed = new DeviceMessage(message);
            if(onMessageReceived != null) {
                onMessageReceived.messageReceived(parsed.toString().getBytes());
            }

            if(settings.getRequestLogger() != null) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                JsonDoc msg = new JsonDoc();
                msg.set("timestamp", timestamp.toString());
                msg.set("type", "RESPONSE");
                msg.set("message",parsed.toString());

                settings.getRequestLogger().ResponseReceived(msg.toString());
            }
        }
        catch(IOException exc) {
            /* Logging should never interfere with processing */
        }
    }
}
