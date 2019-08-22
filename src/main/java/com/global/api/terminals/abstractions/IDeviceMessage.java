package com.global.api.terminals.abstractions;

public interface IDeviceMessage {
    boolean isKeepAlive();
    void setKeepAlive(boolean keepAlive);
    boolean isAwaitResponse();
    void setAwaitResponse(boolean awaitResponse);
    byte[] getSendBuffer();
}
