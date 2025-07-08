package com.global.api.terminals.upa.interfaces;

import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.DeviceCommInterface;
import com.global.api.terminals.abstractions.IDeviceMessage;
import com.global.api.terminals.abstractions.ITerminalConfiguration;

import java.util.concurrent.CountDownLatch;

public class UpaAidlInterface extends DeviceCommInterface {
    public UpaAidlInterface(ITerminalConfiguration settings) {
        super(settings);
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
        String deviceMessage;
        CountDownLatch latch = new CountDownLatch(1);

        try {
            deviceMessage = message.toString(true);

            raiseOnMessageSent(deviceMessage);


            if (settings.getAidlService() != null) {
                settings.getAidlService().onSendAidlMessage(deviceMessage, data -> {
                    response[0] = data;
                    raiseOnMessageReceived(data.getBytes());
                    latch.countDown();
                });
            }
            latch.await();
            String callbackResponse = response[0];
            return callbackResponse.getBytes();
        } catch (Exception e) {
            throw new MessageException(e.getMessage(), e);
        }
    }
}
