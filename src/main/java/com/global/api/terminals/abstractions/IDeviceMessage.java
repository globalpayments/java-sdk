package com.global.api.terminals.abstractions;

import com.global.api.utils.IRawRequestBuilder;

public interface IDeviceMessage {
    boolean isKeepAlive();
    void setKeepAlive(boolean keepAlive);
    boolean isAwaitResponse();
    void setAwaitResponse(boolean awaitResponse);
    byte[] getSendBuffer();
    IRawRequestBuilder getRequestBuilder();
}
