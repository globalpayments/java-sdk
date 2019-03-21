package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE127_ForwardingDataTag implements IStringConstant {
    E3_EncryptedData("E3E"),
    ForwardedHeaderOnly("HDR"),
    ForwardedRequest("REQ"),
    ForwardedResponse("RSP");

    private final String value;
    DE127_ForwardingDataTag(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
