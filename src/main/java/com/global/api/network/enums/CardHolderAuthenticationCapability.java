package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum CardHolderAuthenticationCapability implements IStringConstant, IMappedConstant {
    None("0", new HashMap<Target, String>() {{
        put(Target.Transit, "NO_CAPABILITY");
    }}),
    PIN("1", new HashMap<Target, String>() {{
        put(Target.Transit, "PIN_ENTRY");
    }}),
    ElectronicSignature("2", new HashMap<Target, String>() {{
        put(Target.Transit, "SIGNATURE_ANALYSIS");
    }}),
    Biometrics("3", new HashMap<Target, String>() {{
        put(Target.Transit, "BIOMETRICS");
    }}),
    Biographic("4", new HashMap<Target, String>() {{
        put(Target.Transit, "BIOGRAPHIC");
    }}),
    ElectronicAuthenticationInoperable("5", new HashMap<Target, String>() {{
        put(Target.Transit, "SIGNATURE_ANALYSIS_INOPERATIVE");
    }}),
    Other("6", new HashMap<Target, String>() {{
        put(Target.Transit, "OTHER");
    }}),
    OnCardSecurityCode("9", new HashMap<Target, String>() {{
        put(Target.Transit, "ON_CARD_SECURITY_CODE");
    }}),
    ElectronicAuthentication("S", new HashMap<Target, String>() {{
        put(Target.Transit, "ELECTRONIC_AUTHENTICATION");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    CardHolderAuthenticationCapability(String value, HashMap<Target, String> transitValue) {
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
