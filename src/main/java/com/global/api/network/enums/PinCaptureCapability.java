package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum PinCaptureCapability implements IStringConstant {
    None("0"),
    Unknown("1"),
    FourCharacters("4"),
    FiveCharacters("5"),
    SixCharacters("6"),
    SevenCharacters("7"),
    EightCharacters("8"),
    NineCharacters("9"),
    TenCharacters("A"),
    ElevenCharacters("B"),
    TwelveCharacters("C");

    private final String value;
    PinCaptureCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
