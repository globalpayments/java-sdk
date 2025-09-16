package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum TerminalOutputCapability implements IStringConstant, IMappedConstant {
    Unknown("0", new HashMap<Target, String>() {{
        put(Target.Transit, "UNKNOWN");
    }}),
    None("1", new HashMap<Target, String>() {{
        put(Target.Transit, "NONE");
    }}),
    Printing("2", new HashMap<Target, String>() {{
        put(Target.Transit, "PRINT_ONLY");
    }}),
    Display("3", new HashMap<Target, String>() {{
        put(Target.Transit, "DISPLAY_ONLY");
    }}),
    Printing_Display("4", new HashMap<Target, String>() {{
        put(Target.Transit, "PRINT_AND_DISPLAY");
    }}),
    Coupon_Printing("9", new HashMap<Target, String>() {{
        put(Target.Transit, "COUPON_PRINTING");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    TerminalOutputCapability(String value, HashMap<Target, String> transitValue) {
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
