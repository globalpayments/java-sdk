package com.global.api.network.enums;

import com.global.api.entities.enums.IByteConstant;

public enum  NetworkResponseCode implements IByteConstant {
    Success(0x00),
    FailedConnection(0x01),
    Timeout(0x02),
    FormatError_Originator(0x03),
    StoreAndForward(0x04),
    UnsupportedTransaction(0x05),
    UnsupportedServiceProvider(0x06),
    FormatError_ServiceProvider(0x07);

    private final byte value;
    NetworkResponseCode(int value) { this.value = (byte)value; }
    public byte getByte() {
        return this.value;
    }
}
