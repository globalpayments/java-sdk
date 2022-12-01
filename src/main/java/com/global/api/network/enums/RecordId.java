package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum RecordId implements IStringConstant {
    E3_Encryption("E3"),
    Encryption_3DE("3D");

    String value;
    RecordId(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}

