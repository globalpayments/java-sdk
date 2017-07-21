package com.global.api.terminals.abstractions;

public interface IBatchCloseResponse extends IDeviceResponse {
    String getTotalCount();
    String getTotalAmount();
}
