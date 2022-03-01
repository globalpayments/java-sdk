package com.global.api.entities.enums;

import java.util.HashMap;

public enum AccountType implements IMappedConstant {
    Checking(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "CHECKING");
        put(Target.GP_API, "CHECKING");
    }}),

    Savings(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "SAVINGS");
        put(Target.GP_API, "SAVING");
    }}),

    Credit(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "CREDIT");
        put(Target.GP_API, "CREDIT");
    }});

    HashMap<Target, String> value;
    AccountType(HashMap<Target, String> value){
        this.value = value;
    }

    public String getValue() {
        if(value.containsKey(Target.DEFAULT)) {
            return this.value.get(Target.DEFAULT);
        }
        return null;
    }

    @Override
    public String getValue(Target target) {
        if(value.containsKey(target)) {
            return this.value.get(target);
        }
        if (value.containsKey(Target.DEFAULT)) {
            return this.value.get(Target.DEFAULT);
        }
        return null;
    }
}
