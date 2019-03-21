package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum FallbackCode implements IStringConstant {
    None("00"),
    CreditAdjustment("08"),
    Referral("30"),
    Received_SystemMalfunction("48"),
    CouldNotCommunicateWithHost("68"),
    Received_IssuerTimeout("88"),
    Received_IssuerUnavailable("98");

    private final String value;
    FallbackCode(String value) { this.value = value; }

    public byte[] getBytes() {
        return value.getBytes();
    }

    public String getValue() {
        return value;
    }
}
