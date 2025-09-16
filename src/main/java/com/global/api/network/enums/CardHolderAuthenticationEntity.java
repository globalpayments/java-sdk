package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum CardHolderAuthenticationEntity implements IStringConstant, IMappedConstant {
    NotAuthenticated("0", new HashMap<Target, String>() {{
        put(Target.Transit, "NOT_AUTHENTICATED");
    }}),
    ICC("1", new HashMap<Target, String>() {{
        put(Target.Transit, "ICC_OFFLINE_PIN");
    }}),
    CAD("2", new HashMap<Target, String>() {{
        put(Target.Transit, "CARD_ACCEPTANCE_DEVICE");
    }}),
    AuthorizingAgent("3", new HashMap<Target, String>() {{
        put(Target.Transit, "AUTHORIZING_AGENT_ONLINE_PIN");
    }}),
    ByMerchant("4", new HashMap<Target, String>() {{
        put(Target.Transit, "MERCHANT_CARD_ACCEPTOR_SIGNATURE");
    }}),
    Other("5", new HashMap<Target, String>() {{
        put(Target.Transit, "OTHER");
    }}),
    CallCenter("8", new HashMap<Target, String>() {{
        put(Target.Transit, "CALL_CENTER");
    }}),
    CardIssuer("9", new HashMap<Target, String>() {{
        put(Target.Transit, "CARD_ISSUER");
    }});

    private final String value;
    private final HashMap<Target, String> transitValue;
    
    CardHolderAuthenticationEntity(String value, HashMap<Target, String> transitValue) {
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
