package com.global.api.entities.enums;

import java.util.HashMap;

public enum TaxCategory implements IStringConstant, IMappedConstant {
    Service("SERVICE", new HashMap<Target, String>() {{
        put(Target.Transit, "SERVICE");
    }}),

    Duty("DUTY", new HashMap<Target, String>() {{
        put(Target.Transit, "DUTY");
    }}),

    VAT("VAT", new HashMap<Target, String>() {{
        put(Target.Transit, "VAT");
    }}),

    Alternate("ALTERNATE", new HashMap<Target, String>() {{
        put(Target.Transit, "ALTERNATE");
    }}),

    National("NATIONAL", new HashMap<Target, String>() {{
        put(Target.Transit, "NATIONAL");
    }}),

    TaxExempt("TAX_EXEMPT", new HashMap<Target, String>() {{
        put(Target.Transit, "TAX_EXEMPT");
    }});

    private final HashMap<Target, String> transitValue;
    private final String value;

    TaxCategory(String value, HashMap<Target, String> transitValue) {
        this.value = value;
        this.transitValue = transitValue;
    }

    public String getValue() {
        return this.value;
    }

    public String getValue(Target target) {
        if (transitValue.containsKey(target)) {
            return transitValue.get(target);
        }
        return null;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }
}