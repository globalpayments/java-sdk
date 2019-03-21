package com.global.api.network.enums;

import com.global.api.entities.enums.IByteConstant;

public enum NetworkProcessingFlag implements IByteConstant {
    NonPersistentConnection(0x00),
    PersistentConnection(0x01);

    private final byte value;
    NetworkProcessingFlag(int value) { this.value = (byte)value; }
    public byte getByte() {
        return this.value;
    }
}
