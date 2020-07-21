package com.global.api.entities.enums;
import java.util.HashMap;

public enum StoredCredentialInitiator implements IMappedConstant {
    CardHolder(new HashMap<Target, String>() {{
        put(Target.Realex, "cardholder");
        put(Target.Portico, "C");
    }}),
    Merchant(new HashMap<Target, String>() {{
        put(Target.Realex, "merchant");
        put(Target.Portico, "M");
    }}),
    Scheduled(new HashMap<Target, String>() {{
        put(Target.Realex, "scheduled");
    }});

    HashMap<Target, String> value;
    StoredCredentialInitiator(HashMap<Target, String> value){
        this.value = value;
    }
    public byte[] getBytes(Target target) {
        if(value.containsKey(target)) {
            return this.value.get(target).getBytes();
        }
        return null;
    }
    public String getValue(Target target) {
        if(value.containsKey(target)) {
            return this.value.get(target);
        }
        return null;
    }
}
