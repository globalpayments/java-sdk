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

public abstract class DeviceController implements IDisposable {
    protected ITerminalConfiguration settings;
    protected IDeviceCommInterface _interface;
    protected IRequestIdProvider requestIdProvider;
    protected IRequestLogger logManagementProvider;

    public DeviceController(ITerminalConfiguration settings) throws ConfigurationException {
        settings.validate();
        this.settings = settings;
        this.requestIdProvider = settings.getRequestIdProvider();
        this.logManagementProvider = settings.getLogManagementProvider();
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

    public byte[] send(IDeviceMessage message) throws ApiException {
        if (_interface != null)
            return _interface.send(message);
        return null;
    }

    public abstract IDeviceInterface configureInterface() throws ConfigurationException;

    public abstract TerminalResponse processTransaction(TerminalAuthBuilder builder) throws ApiException;

    public abstract TerminalResponse manageTransaction(TerminalManageBuilder builder) throws ApiException;

    public abstract ITerminalReport processReport(TerminalReportBuilder builder) throws ApiException;

    public void dispose() {
        if (_interface != null)
            _interface.disconnect();
    }
}
