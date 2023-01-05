package com.global.api.entities.enums;

import java.util.HashMap;

public enum BNPLType implements IMappedConstant {
    AFFIRM(new HashMap<Target, String>() {{
        put(Target.GP_API, "AFFIRM");
    }}),

    CLEARPAY(new HashMap<Target, String>() {{
        put(Target.GP_API, "CLEARPAY");
    }}),

    KLARNA(new HashMap<Target, String>() {{
        put(Target.GP_API, "KLARNA");
    }});

    HashMap<Target, String> value;

    BNPLType(HashMap<Target, String> value){
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