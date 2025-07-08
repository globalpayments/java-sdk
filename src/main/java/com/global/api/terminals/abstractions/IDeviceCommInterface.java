package com.global.api.terminals.abstractions;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public interface IDeviceCommInterface {
    void connect();
    void disconnect();
    byte[] send(IDeviceMessage message) throws ApiException;
    void setMessageSentHandler(IMessageSentInterface messageInterface);
    void setMessageReceivedHandler(IMessageReceivedInterface messageInterface);
}
