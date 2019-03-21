package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum CardHolderAuthenticationEntity implements IStringConstant {
    NotAuthenticated("0"),
    ICC("1"),
    CAD("2"),
    AuthorizingAgent("3"),
    ByMerchant("4"),
    Other("5"),
    CallCenter("8"),
    CardIssuer("9");

    private final String value;
    CardHolderAuthenticationEntity(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
