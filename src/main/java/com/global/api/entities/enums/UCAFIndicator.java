package com.global.api.entities.enums;

import java.util.HashMap;

public enum UCAFIndicator implements IStringConstant, IMappedConstant {

    NotSupported("0", new HashMap<Target, String>() {{
        put(Target.Transit, "0");
    }}),
    MerchantOnly("1", new HashMap<Target, String>() {{
        put(Target.Transit, "1");
    }}),
    FullyAuthenticated("2", new HashMap<Target, String>() {{
        put(Target.Transit, "2");
    }}),
    IssuerRiskBased("5", new HashMap<Target, String>() {{
        put(Target.Transit, "5");
    }}),
    MerchantRiskBased("6", new HashMap<Target, String>() {{
        put(Target.Transit, "6");
    }}),
    PartialShipmentIncremental("7", new HashMap<Target, String>() {{
        put(Target.Transit, "7");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;

    UCAFIndicator(String value, HashMap<Target, String> transitValue) {
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