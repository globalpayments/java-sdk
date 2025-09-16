package com.global.api.network.enums;


import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum CardDataOutputCapability implements IStringConstant, IMappedConstant {
    Unknown("0", new HashMap<Target, String>() {{
        put(Target.Transit, "OTHER");
    }}),
    None("1", new HashMap<Target, String>() {{
        put(Target.Transit, "NONE");
    }}),
    MagStripe_Write("2", new HashMap<Target, String>() {{
        put(Target.Transit, "MAGNETIC_STRIPE_WRITE");
    }}),
    ICC("3", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    CardDataOutputCapability(String value, HashMap<Target, String> transitValue) {
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
