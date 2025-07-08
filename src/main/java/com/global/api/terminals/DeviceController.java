package com.global.api.terminals;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.logging.IRequestLogger;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.messaging.IMessageReceivedInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public abstract class DeviceController implements IDisposable {
    protected ITerminalConfiguration settings;
    protected IDeviceInterface _device;
    protected IDeviceCommInterface _interface;
    protected IRequestIdProvider requestIdProvider;
    protected IRequestLogger requestLogger;

    protected IMessageSentInterface onMessageSent;
    protected IMessageReceivedInterface onMessageReceived;

    protected void setOnMessageSent(IMessageSentInterface onMessageSent) {
        this.onMessageSent = onMessageSent;
    }
    protected void setOnMessageReceived(IMessageReceivedInterface onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public ConnectionModes getConnectionModes() {
        if (settings != null)
            return settings.getConnectionMode();
        return null;
    }

    public DeviceType getDeviceType() {
        if (settings != null)
            return settings.getDeviceType();
        return null;
    }

    public DeviceController(ITerminalConfiguration settings) throws ConfigurationException {
        settings.validate();
        this.settings = settings;
        this.requestIdProvider = settings.getRequestIdProvider();
        this.requestLogger = settings.getRequestLogger();

        generateInterface();
        if(_interface != null) {
            _interface.setMessageSentHandler(message -> {
                if (onMessageSent != null)
                    onMessageSent.messageSent(message);
            });
            _interface.setMessageReceivedHandler(message -> {
                if (onMessageReceived != null)
                    onMessageReceived.messageReceived(message);
            });
        }
    }

    public byte[] send(IDeviceMessage message) throws ApiException {
        if (_interface != null)
            return _interface.send(message);
        return null;
    }

    public IDeviceInterface configureInterface() throws ConfigurationException {
        if(_device == null) {
            generateInterface();
        }
        return _device;
    }

    protected abstract void generateInterface() throws ConfigurationException;

    public abstract TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException;

    public abstract TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException;

    public abstract ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException;

    public void dispose() {
        if (_interface != null)
            _interface.disconnect();
    }
}
