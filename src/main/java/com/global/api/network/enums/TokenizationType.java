package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum TokenizationType implements IStringConstant {

    GlobalTokenization("1"),
    MerchantTokenization("2");
    String value;
    TokenizationType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
