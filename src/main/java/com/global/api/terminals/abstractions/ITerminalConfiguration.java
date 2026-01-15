package com.global.api.terminals.abstractions;

import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.serviceConfigs.Configuration;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.logging.IRequestLogger;
import com.global.api.terminals.IRequestIdProvider;

public interface ITerminalConfiguration {
    /** GENERIC SETTINGS **/

    ConnectionModes getConnectionMode();

    void setConnectionMode(ConnectionModes connectionMode);

    int getTimeout();

    Configuration setTimeout(int timeout);

    void validate() throws ConfigurationException;

    DeviceType getDeviceType();

    void setDeviceType(DeviceType type);

    IRequestIdProvider getRequestIdProvider();

    void setRequestIdProvider(IRequestIdProvider requestIdProvider);

    IRequestLogger getRequestLogger();

    Configuration setRequestLogger(IRequestLogger requestLogger);

    /** TCP CONNECTION SETTINGS **/

    String getIpAddress();

    void setIpAddress(String ipAddress);

    int getPort();

    void setPort(int port);

    /** SERIAL CONNECTION SETTINGS **/

    BaudRate getBaudRate();

    void setBaudRate(BaudRate baudRate);

    Parity getParity();

    void setParity(Parity parity);

    StopBits getStopBits();

    void setStopBits(StopBits stopBits);

    DataBits getDataBits();

    void setDataBits(DataBits dataBits);

    /** MITC CONNECTION SETTINGS **/

    GatewayConfig getGatewayConfig();

    void setGatewayConfig(GatewayConfig gatewayConfig);

    /** DIAMOND CLOUD SETTINGS **/
    String getSecretKey();
    void setSecretKey(String secretKey);

    String getIsvId();
    void setIsvId(String isvId);

    String getServiceUrl();
    Configuration setServiceUrl(String serviceUrl);

    String getPosId();
    void setPosId(String posId);

    String getRegion();
    void setRegion(String region);

    /** AIDL SETTINGS **/
    IAidlService getAidlService();
    void setAidlService(IAidlService service);
}
