package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE22_CardHolderAuthenticationMethod implements IStringConstant {
    NotAuthenticated("0"),
    PIN("1"),
    ElectronicSignature("2"),
    Biometrics("3"),
    Biographic("4"),
    ManualSignatureVerification("5"),
    Other("6"),
    OnCard_SecurityCode("9"),
    Authenticated("S"),
    AuthenticationAttempted("T");

    private final String value;
    DE22_CardHolderAuthenticationMethod(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
