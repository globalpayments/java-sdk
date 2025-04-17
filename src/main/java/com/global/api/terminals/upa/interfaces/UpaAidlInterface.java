package com.global.api.terminals.upa.interfaces;

import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IAidlCallback;
import com.global.api.terminals.abstractions.IDeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.IUPAMessage;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

public class UpaAidlInterface implements IDeviceCommInterface, IUPAMessage {

    private final ConnectionConfig configs;
    private boolean dataReceived = false;
    private IMessageSentInterface onMessageSent;
    private IMessageReceivedInterface onMessageReceived;

    public UpaAidlInterface(ConnectionConfig configs) {
        this.configs = configs;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public byte[] send(IDeviceMessage message) throws MessageException {
        final String[] response = {""};
        String deviceMessage = "";
        CountDownLatch latch = new CountDownLatch(1);

        try {
            deviceMessage = message.toString(true);

            if (onMessageSent != null) {
                long currentMillis = System.currentTimeMillis();
                Timestamp t = new Timestamp(currentMillis);
                onMessageSent.messageSent(t + ":\n" + deviceMessage);
            }


            if (configs.getIAidlService() != null) {
                configs.getIAidlService().onSendAidlMessage(deviceMessage, new IAidlCallback() {
                    @Override
                    public void onResponse(String data) {
                        response[0] = data;
                        if (onMessageReceived != null) {
                            onMessageReceived.messageReceived(data.getBytes());
                        }
                        latch.countDown();
                    }
                });
            }
            latch.await();
            String callbackResponse = response[0].toString();
            return callbackResponse.getBytes();
        } catch (Exception e) {
            throw new MessageException(e.getMessage(), e);
        }
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
