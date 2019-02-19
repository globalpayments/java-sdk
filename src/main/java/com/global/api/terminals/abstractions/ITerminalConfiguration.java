package com.global.api.terminals.abstractions;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.IRequestIdProvider;

public interface ITerminalConfiguration {
    ConnectionModes getConnectionMode();
    void setConnectionMode(ConnectionModes connectionMode);
    String getIpAddress();
    void setIpAddress(String ipAddress);
    int getPort();
    void setPort(int port);
    BaudRate getBaudRate();
    void setBaudRate(BaudRate baudRate);
    Parity getParity();
    void setParity(Parity parity);
    StopBits getStopBits();
    void setStopBits(StopBits stopBits);
    DataBits getDataBits();
    void setDataBits(DataBits dataBits);
    int getTimeout();
    void setTimeout(int timeout);
    void validate() throws ConfigurationException;
    DeviceType getDeviceType();
    void setDeviceType(DeviceType type);
    IRequestIdProvider getRequestIdProvider();
    void setRequestIdProvider(IRequestIdProvider requestIdProvider);
}
