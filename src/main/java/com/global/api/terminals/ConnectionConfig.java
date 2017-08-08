package com.global.api.terminals;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.utils.StringUtils;

public class ConnectionConfig implements ITerminalConfiguration {
    private ConnectionModes connectionMode;
    private BaudRate baudRate;
    private Parity parity;
    private StopBits stopBits;
    private DataBits dataBits;
    private int timeout;
    private String ipAddress;
    private int port;
    private DeviceType deviceType;

    public ConnectionModes getConnectionMode() {
        return connectionMode;
    }
    public void setConnectionMode(ConnectionModes connectionModes) {
        this.connectionMode = connectionModes;
    }
    public BaudRate getBaudRate() {
        return baudRate;
    }
    public void setBaudRate(BaudRate baudRate) {
        this.baudRate = baudRate;
    }
    public Parity getParity() {
        return parity;
    }
    public void setParity(Parity parity) {
        this.parity = parity;
    }
    public StopBits getStopBits() {
        return stopBits;
    }
    public void setStopBits(StopBits stopBits) {
        this.stopBits = stopBits;
    }
    public DataBits getDataBits() {
        return dataBits;
    }
    public void setDataBits(DataBits dataBits) {
        this.dataBits = dataBits;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public DeviceType getDeviceType() {
        return deviceType;
    }
    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public ConnectionConfig(){
        setTimeout(30000);
    }

    public void validate() throws ConfigurationException {
        if(connectionMode == ConnectionModes.TCP_IP || connectionMode == ConnectionModes.HTTP) {
            if(StringUtils.isNullOrEmpty(ipAddress))
                throw new ConfigurationException("IpAddress is required for TCP or HTTP communication modes.");
            if(port == 0)
                throw new ConfigurationException("Port is required for TCP or HTTP communication modes.");
        }
    }
}
