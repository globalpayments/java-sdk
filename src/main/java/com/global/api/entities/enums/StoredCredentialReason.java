package com.global.api.entities.enums;
import java.util.HashMap;

public enum StoredCredentialReason implements IMappedConstant {
    Incremental(new HashMap<Target, String>() {{
        put(Target.GP_API, "INCREMENTAL");
    }}),
    Resubmission(new HashMap<Target, String>() {{
        put(Target.GP_API, "RESUBMISSION");
    }}),
    Reauthorization(new HashMap<Target, String>() {{
        put(Target.GP_API, "REAUTHORIZATION");
    }}),
    Delayed(new HashMap<Target, String>() {{
        put(Target.GP_API, "DELAYED");
    }}),
    NoShow(new HashMap<Target, String>() {{
        put(Target.GP_API, "NO_SHOW");
    }});

    HashMap<Target, String> value;
    StoredCredentialReason(HashMap<Target, String> value){
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
