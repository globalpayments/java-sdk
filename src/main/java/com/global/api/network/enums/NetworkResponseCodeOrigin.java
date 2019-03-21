package com.global.api.network.enums;

import com.global.api.entities.enums.IByteConstant;

public enum NetworkResponseCodeOrigin implements IByteConstant {
    Default(0x00),
    FrontEndProcess(0x01),
    BackEndProcess(0x02),
    InternalProcess(0x03),
    AuthorizationHost(0x04);

    private final byte value;
    NetworkResponseCodeOrigin(int value) { this.value = (byte)value; }
    public byte getByte() {
        return this.value;
    }
}
