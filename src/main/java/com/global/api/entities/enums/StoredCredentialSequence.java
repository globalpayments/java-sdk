package com.global.api.entities.enums;

import java.util.HashMap;

public enum StoredCredentialSequence implements IMappedConstant {
    First(new HashMap<Target, String>() {{
        put(Target.Realex, "first");
        put(Target.GP_API, "FIRST");
    }}),
    Subsequent(new HashMap<Target, String>() {{
        put(Target.Realex, "subsequent");
        put(Target.GP_API, "SUBSEQUENT");
    }}),
    Last(new HashMap<Target, String>() {{
        put(Target.GP_API, "LAST");
    }});

    HashMap<Target, String> value;
    StoredCredentialSequence(HashMap<Target, String> value){
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