package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE22_CardDataInputMode implements IStringConstant {
    Unspecified("0"),
    Manual("1"),
    MagStripe("2"),
    BarCode("3"),
    OCR("4"),
    MagStripe_Fallback("5"),
    KeyEntry("6"),
    ContactlessMsd("A"),
    UnalteredTrackData("B"),
    ContactEmv("C"),
    ContactlessEmv("D"),
    CredentialOnFile("S"),
    Ecommerce("T"),
    SecureEcommerce("U");

    private final String value;
    DE22_CardDataInputMode(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
