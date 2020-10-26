package com.global.api.terminals.abstractions;

import com.global.api.entities.enums.BaudRate;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DataBits;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.Parity;
import com.global.api.entities.enums.StopBits;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.IRequestIdProvider;

public interface ITerminalConfiguration {
    ConnectionModes getConnectionMode();
    void setConnectionMode(ConnectionModes connectionMode);
    String getIpAddress();
    void setIpAddress(String ipAddress);
    String getPort();
    void setPort(String port);
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
