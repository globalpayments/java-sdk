package com.global.api.entities.enums;

import java.util.HashMap;

public enum EmvLastChipRead implements IMappedConstant {
    SUCCESSFUL(new HashMap<Target, String>() {{
        put(Target.Transit, "SUCCESSFUL");
    }}),
    FAILED(new HashMap<Target, String>() {{
        put(Target.Transit, "FAILED");
    }}),
    NOT_A_CHIP_TRANSACTION(new HashMap<Target, String>() {{
        put(Target.Transit, "NOT_A_CHIP_TRANSACTION");
    }}),
    UNKNOWN(new HashMap<Target, String>() {{
        put(Target.Transit, "UNKNOWN");
    }});

    private final HashMap<Target, String> transitValue;
    
    EmvLastChipRead(HashMap<Target, String> transitValue) {
        this.transitValue = transitValue;
    }
    
    public String getValue(Target target) {
        if (transitValue.containsKey(target)) {
            return transitValue.get(target);
        }
        return null;
    }
}
