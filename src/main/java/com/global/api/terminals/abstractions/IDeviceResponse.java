package com.global.api.terminals.abstractions;

public interface IDeviceResponse {
    String getStatus();
    void setStatus(String status);
    String getCommand();
    void setCommand(String command);
    String getVersion();
    void setVersion(String version);
    String getDeviceResponseCode();
    void setDeviceResponseCode(String deviceResponseCode);
    String getDeviceResponseText();
    void setDeviceResponseText(String deviceResponseMessage);
    String toString();
}
