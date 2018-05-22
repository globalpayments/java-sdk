package com.global.api.terminals.abstractions;

public interface ISignatureResponse extends IDeviceResponse {
    byte[] getSignatureData();
}
