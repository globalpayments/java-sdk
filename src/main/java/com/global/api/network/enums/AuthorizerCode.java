package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum AuthorizerCode implements IStringConstant {
    Interchange_Authorized(" "),
    Host_Authorized("B"),
    Terminal_Authorized("T"),
    Voice_Authorized("V"),
    PassThrough("P"),
    NegativeFile("N"),
    LocalNegativeFile("L"),
    AuthTable("A"),
    ReservedAuthorized("D"),
    Synchrony("S"),
    Chase_Net("C"),
    GlobalPayments_Gift("G");

    private final String value;
    AuthorizerCode(String value) { this.value = value; }

    public byte[] getBytes() {
        return value.getBytes();
    }

    public String getValue() {
        return value;
    }
}
