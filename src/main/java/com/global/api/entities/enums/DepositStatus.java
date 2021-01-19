package com.global.api.entities.enums;
import com.global.api.entities.enums.IMappedConstant;
import com.global.api.entities.enums.Target;

import java.util.HashMap;

public enum DepositStatus implements IMappedConstant {
    Funded(new HashMap<Target, String>() {{
        put(Target.GP_API, "FUNDED");
    }}),
    SplitFunding(new HashMap<Target, String>() {{
        put(Target.GP_API, "SPLIT FUNDING");
    }}),
    Delayed(new HashMap<Target, String>() {{
        put(Target.GP_API, "DELAYED");
    }}),
    Reserved(new HashMap<Target, String>() {{
        put(Target.GP_API, "RESERVED");
    }}),
    Irregular(new HashMap<Target, String>() {{
        put(Target.GP_API, "IRREG");
    }}),
    Released(new HashMap<Target, String>() {{
        put(Target.GP_API, "RELEASED");
    }});

    HashMap<Target, String> value;
    DepositStatus(HashMap<Target, String> value){
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