package com.global.api.entities.enums;

import java.util.HashMap;

public enum ReasonCode implements IMappedConstant {
    Fraud(new HashMap<Target, String>() {{
        put(Target.GP_API, "FRAUD");
    }}),
    FalsePositive(new HashMap<Target, String>() {{
        put(Target.GP_API, "FALSE_POSITIVE");
    }}),
    OutOfStock(new HashMap<Target, String>() {{
        put(Target.GP_API, "OUT_OF_STOCK");
    }}),
    InStock(new HashMap<Target, String>() {{
        put(Target.GP_API, "IN_STOCK");
    }}),
    Other(new HashMap<Target, String>() {{
        put(Target.GP_API, "OTHER");
    }}),
    NotGiven(new HashMap<Target, String>() {{
        put(Target.GP_API, "NOT_GIVEN");
    }});

    HashMap<Target, String> value;
    ReasonCode(HashMap<Target, String> value){
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
