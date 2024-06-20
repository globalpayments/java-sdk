package com.global.api.terminals.abstractions;

import com.global.api.terminals.messaging.IMessageReceivedInterface;

public interface IUPAMessage {
    void setMessageReceivedHandler(IMessageReceivedInterface onMessageReceived);
}
