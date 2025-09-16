package com.global.api.entities.enums;

import java.util.HashMap;

public enum EmvFallbackCondition implements IStringConstant, IMappedConstant {
    ChipReadFailure("ICC_TERMINAL_ERROR", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_TERMINAL_ERROR");
    }}),
    NoCandidateList("NO_CANDIDATE_LIST", new HashMap<Target, String>() {{
        put(Target.Transit, "NO_CANDIDATE_LIST");
    }});

    private String value;
    private final HashMap<Target, String> transitValue;
    
    EmvFallbackCondition(String value, HashMap<Target, String> transitValue) {
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
