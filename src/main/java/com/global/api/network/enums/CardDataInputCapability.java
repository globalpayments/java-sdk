package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum CardDataInputCapability implements IStringConstant {
    Unknown("0"),
    Manual("1"),
    MagStripe("2"),
    BarCode("3"),
    OCR("4"),
    ContactEmv("5"),
    KeyEntry("6"),
    ContactlessEmv("9"),
    ContactlessMsd("A"),
    MagStripe_KeyEntrty("B"),
    ContactEmv_MagStripe_KeyEntry("C"),
    ContactEmv_MagStripe("D"),
    ContactEmc_KeyEntry("E"),
    ContactlessMsd_KeyEntry("F"),
    ContactlessMsd_MagStripe("G"),
    ContactlessMsd_MagStripe_KeyEntry("H"),
    ContactEmv_ContactlessMsd("I"),
    ContactEmv_ContactlessMsd_KeyEntry("J"),
    ContactEmv_ContactlessMsd_MagStripe("K"),
    ContactEmv_ContactlessMsd_MagStripe_KeyEntry("L"),
    ContactlessEmv_KeyEntry("M"),
    ContactlessEmv_MagStripe("N"),
    ContactlessEmv_ContactlessMsd("O"),
    ContactlessEmv_ContactlessMsd_KeyEntry("P"),
    ContactlessEmv_ContactlessMsd_MagStripe("Q"),
    ContactlessEmv_ContactlessMsd_MagStripe_KeyEntry("R"),
    ContactlessEmv_MagStripe_KeyEntry("S"),
    ContactlessEmv_ContactEmv("T"),
    ContactlessEmv_ContactEmv_KeyEntry("U"),
    ContactlessEmv_ContactEmv_MagStripe("V"),
    ContactlessEmv_ContactEmv_MagStripe_KeyEntry("W"),
    ContactlessEmv_ContactEmv_ContactlessMsd("X"),
    ContactlessEmv_ContactEmv_ContactlessMsd_KeyEntry("Y"),
    ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe("Z"),
    ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry("a");

    private final String value;
    CardDataInputCapability(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
