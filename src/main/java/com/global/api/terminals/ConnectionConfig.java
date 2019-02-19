package com.global.api.terminals;

import com.global.api.ConfiguredServices;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.serviceConfigs.Configuration;
import com.global.api.terminals.abstractions.ITerminalConfiguration;
import com.global.api.terminals.hpa.HpaController;
import com.global.api.terminals.pax.PaxController;
import com.global.api.utils.StringUtils;

public class ConnectionConfig extends Configuration implements ITerminalConfiguration {
    private ConnectionModes connectionMode;
    private BaudRate baudRate;
    private Parity parity;
    private StopBits stopBits;
    private DataBits dataBits;
    private String ipAddress;
    private int port;
    private DeviceType deviceType;
    private IRequestIdProvider requestIdProvider;

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
    public IRequestIdProvider getRequestIdProvider() {
		return requestIdProvider;
	}
    public void setRequestIdProvider(IRequestIdProvider requestIdProvider) {
		this.requestIdProvider = requestIdProvider;
	}

    public ConnectionConfig(){
        timeout = 30000;
    }

    public void configureContainer(ConfiguredServices services) throws ConfigurationException {
        switch (deviceType) {
            case PAX_S300:
                services.setDeviceController(new PaxController(this));
                break;
            case HPA_ISC250:
                services.setDeviceController(new HpaController(this));
            default:
                break;
        }
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
