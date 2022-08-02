package com.global.api.entities.enums;

import java.util.HashMap;

public enum PayLinkSortProperty implements IMappedConstant {

    TimeCreated(new HashMap<Target, String>() {{
        put(Target.GP_API, "TIME_CREATED");
    }});

    HashMap<Target, String> value;

    PayLinkSortProperty(HashMap<Target, String> value){
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