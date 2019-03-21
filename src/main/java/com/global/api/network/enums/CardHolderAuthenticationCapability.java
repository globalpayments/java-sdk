package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum CardHolderAuthenticationCapability implements IStringConstant {
    None("0"),
    PIN("1"),
    ElectronicSignature("2"),
    Biometrics("3"),
    Biographic("4"),
    ElectronicAuthenticationInoperable("5"),
    Other("6"),
    OnCardSecurityCode("9"),
    ElectronicAuthentication("S");

    private final String value;
    CardHolderAuthenticationCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
