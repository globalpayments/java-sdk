package com.global.api.network.enums;

import com.global.api.entities.enums.IByteConstant;

public enum CharacterSet implements IByteConstant {
    ASCII (0x01),
    EBCDIC (0x02);

    private final byte value;
    CharacterSet(int value) { this.value = (byte)value; }
    public byte getByte() {
        return this.value;
    }
}
