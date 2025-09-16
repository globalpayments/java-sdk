package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum PinCaptureCapability implements IStringConstant, IMappedConstant {
    None("0", new HashMap<Target, String>() {{
        put(Target.Transit, "NOT_SUPPORTED");
    }}),
    Unknown("1", new HashMap<Target, String>() {{
        put(Target.Transit, "UNKNOWN");
    }}),
    FourCharacters("4", new HashMap<Target, String>() {{
        put(Target.Transit, "4");
    }}),
    FiveCharacters("5", new HashMap<Target, String>() {{
        put(Target.Transit, "5");
    }}),
    SixCharacters("6", new HashMap<Target, String>() {{
        put(Target.Transit, "6");
    }}),
    SevenCharacters("7", new HashMap<Target, String>() {{
        put(Target.Transit, "7");
    }}),
    EightCharacters("8", new HashMap<Target, String>() {{
        put(Target.Transit, "8");
    }}),
    NineCharacters("9", new HashMap<Target, String>() {{
        put(Target.Transit, "9");
    }}),
    TenCharacters("A", new HashMap<Target, String>() {{
        put(Target.Transit, "10");
    }}),
    ElevenCharacters("B", new HashMap<Target, String>() {{
        put(Target.Transit, "11");
    }}),
    TwelveCharacters("C", new HashMap<Target, String>() {{
        put(Target.Transit, "12");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    PinCaptureCapability(String value, HashMap<Target, String> transitValue) {
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
