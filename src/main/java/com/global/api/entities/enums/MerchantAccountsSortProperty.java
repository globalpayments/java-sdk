package com.global.api.entities.enums;

import java.util.HashMap;

public enum MerchantAccountsSortProperty implements IMappedConstant {

    TIME_CREATED(new HashMap<Target, String>() {{
        put(Target.GP_API, "TIME_CREATED");
    }});

    final HashMap<Target, String> value;
    MerchantAccountsSortProperty(HashMap<Target, String> value){
        this.value = value;
    }

    public byte[] getBytes(Target target) {
        if(value.containsKey(target)) {
            return this.value.get(target).getBytes();
        }
        return null;
    }

    @Override
    public String getValue(Target target) {
        if(value.containsKey(target)) {
            return this.value.get(target);
        }
        return null;
    }
}