package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum CardDataInputCapability implements IStringConstant, IMappedConstant {
    Unknown("0", new HashMap<Target, String>() {{
        put(Target.Transit, "UNKNOWN");
    }}),
    Manual("1", new HashMap<Target, String>() {{
        put(Target.Transit, "NO_TERMINAL_MANUAL");
    }}),
    MagStripe("2", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGSTRIPE_READ_ONLY");
    }}),
    BarCode("3", new HashMap<Target, String>() {{
        put(Target.Transit, "BAR_CODE_INPUT");
    }}),
    OCR("4", new HashMap<Target, String>() {{
        put(Target.Transit, "OCR");
    }}),
    ContactEmv("5", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_CHIP_READ_ONLY");
    }}),
    KeyEntry("6", new HashMap<Target, String>() {{
        put(Target.Transit, "KEYED_ENTRY_ONLY");
    }}),
    ContactlessEmv("9", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_CONTACTLESS_ONLY");
    }}),
    ContactlessMsd("A", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGSTRIPE_CONTACTLESS_ONLY");
    }}),
    MagStripe_KeyEntry("B", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGSTRIPE_KEYED_ENTRY_ONLY");
    }}),
    ContactEmv_MagStripe_KeyEntry("C", new HashMap<Target, String>() {{
        put(Target.Transit, "INTEGRATED_CIRCUIT_CARD_CHIP_INPUT");
    }}),
    ContactEmv_MagStripe("D", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGSTRIPE_ICC_ONLY");
    }}),
    ContactEmv_KeyEntry("E", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_KEYED_ENTRY_ONLY");
    }}),
    ContactlessMsd_KeyEntry("F", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_MAGNETIC_STRIPE_INPUT");
    }}),
    ContactlessMsd_MagStripe("G", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_MAGNETIC_STRIPE_INPUT");
    }}),
    ContactlessMsd_MagStripe_KeyEntry("H", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_MAGNETIC_STRIPE_INPUT");
    }}),
    ContactEmv_ContactlessMsd("I", new HashMap<Target, String>() {{
        put(Target.Transit, "INTEGRATED_CIRCUIT_CARD_CHIP_INPUT");
    }}),
    ContactEmv_ContactlessMsd_KeyEntry("J", new HashMap<Target, String>() {{
        put(Target.Transit, "INTEGRATED_CIRCUIT_CARD_CHIP_INPUT");
    }}),
    ContactEmv_ContactlessMsd_MagStripe("K", new HashMap<Target, String>() {{
        put(Target.Transit, "INTEGRATED_CIRCUIT_CARD_CHIP_INPUT");
    }}),
    ContactEmv_ContactlessMsd_MagStripe_KeyEntry("L", new HashMap<Target, String>() {{
        put(Target.Transit, "INTEGRATED_CIRCUIT_CARD_CHIP_INPUT");
    }}),
    ContactlessEmv_KeyEntry("M", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_MagStripe("N", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactlessMsd("O", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactlessMsd_KeyEntry("P", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactlessMsd_MagStripe("Q", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactlessMsd_MagStripe_KeyEntry("R", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    Credential_On_File("S", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGSTRIPE_ICC_KEYED_ENTRY_ONLY");
    }}),
    ECommerce("T", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_CHIP_CONTACT_CONTACTLESS");
    }}),
    Secure_ECommerce("U", new HashMap<Target, String>() {{
        put(Target.Transit, "ELECTRONIC_COMMERCE_SECURE_ELECTRONIC_CHANNEL_ENCRYPTED_SET_WITH_CARDHOLDER_CERTIFICATE");
    }}),
    ContactlessEmv_ContactEmv_MagStripe("V", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactEmv_MagStripe_KeyEntry("W", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactEmv_ContactlessMsd("X", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactEmv_ContactlessMsd_KeyEntry("Y", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe("Z", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }}),
    ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry("a", new HashMap<Target, String>() {{
        put(Target.Transit, "CONTACTLESS_INTEGRATED_CIRCUIT_CARD_INPUT");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    CardDataInputCapability(String value, HashMap<Target, String> transitValue) {
        this.value = value;
        this.transitValue = transitValue;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getValue(Target target) {
        if (transitValue.containsKey(target)) {
            return transitValue.get(target);
        }
        return null;
    }
    
    public byte[] getBytes() {
        return value.getBytes();
    }
}
