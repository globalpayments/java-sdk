package com.global.api.terminals;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.terminals.builders.TerminalReportBuilder;
import com.global.api.terminals.messaging.IBroadcastMessageInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

public abstract class DeviceController implements IDisposable {
	protected ITerminalConfiguration settings;
	protected IDeviceCommInterface _connector;
	protected IDeviceInterface _interface;
    protected IRequestIdProvider requestIdProvider;

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

	public IRequestIdProvider requestIdProvider() {
		if (settings != null)
			return settings.getRequestIdProvider();
		return null;
	}

	private IMessageSentInterface onMessageSent;
	private IBroadcastMessageInterface onBroadcastMessage;

	void setOnMessageSentHandler(IMessageSentInterface onMessageSent) {
		this.onMessageSent = onMessageSent;
	}

	void setOnBroadcastMessageHandler(IBroadcastMessageInterface onBroadcastMessage) {
		this.onBroadcastMessage = onBroadcastMessage;
	}

	public DeviceController(ITerminalConfiguration settings) throws ConfigurationException {
		settings.validate();
		this.settings = settings;
        this.requestIdProvider = settings.getRequestIdProvider();
        
		_connector = configureConnector();
		_connector.setMessageSentHandler(new IMessageSentInterface() {
			public void messageSent(String message) {
				if (onMessageSent != null)
					onMessageSent.messageSent(message);
			}
		});

		_connector.setBroadcastMessageHandler(new IBroadcastMessageInterface() {
			public void broadcastReceived(String code, String message) {
				if (onBroadcastMessage != null)
					onBroadcastMessage.broadcastReceived(code, message);
			}
		});
	}

	public byte[] send(IDeviceMessage message) throws ApiException {
		if (_connector != null) {
			return _connector.send(message);
		}
		return null;
	}

	public abstract IDeviceCommInterface configureConnector() throws ConfigurationException;

	public abstract IDeviceInterface configureInterface() throws ConfigurationException;

	public abstract ITerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException;

	public abstract ITerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException;

	public abstract ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException;

	public void dispose() {
		if (_connector != null) {
			_connector.disconnect();
		}
	}
}
