package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE48_EncryptionAlgorithmDataCode implements IStringConstant {
    NoEncryption("0"),
    DES("1"),
    TripleDES_2Keys("2"),
    TripleDES_3Keys("3");

    private final String value;
    DE48_EncryptionAlgorithmDataCode(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
