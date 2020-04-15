package com.global.api.terminals.abstractions;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public interface IDeviceCommInterface {
    void connect() throws ConfigurationException;
    void disconnect();
    byte[] send(IDeviceMessage message) throws ApiException;
    void setMessageSentHandler(IMessageSentInterface messageInterface);
    void setBroadcastMessageHandler(IBroadcastMessageInterface broadcastInterface);
}
