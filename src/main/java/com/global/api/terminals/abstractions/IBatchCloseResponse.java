package com.global.api.terminals.abstractions;

public interface IBatchCloseResponse extends IDeviceResponse {
    String getTotalAmount();
    String getSequenceNumber();
    String getTotalCount();
}
