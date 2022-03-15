package com.global.api.entities.enums;

import java.util.HashMap;

public enum SdkUiType implements IMappedConstant {

    Text(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "TEXT");
        put(Target.GP_API, "TEXT");
    }}),

    SingleSelect(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "SINGLE_SELECT");
        put(Target.GP_API, "SINGLE_SELECT");
    }}),

    MultiSelect(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "MULTI_SELECT");
        put(Target.GP_API, "MULTI_SELECT");
    }}),

    OOB(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "OOB");
        put(Target.GP_API, "OUT_OF_BAND");
    }}),

    HTML_Other(new HashMap<Target, String>() {{
        put(Target.DEFAULT, "HTML_OTHER");
        put(Target.GP_API, "HTML_OTHER");
    }});

    HashMap<Target, String> value;

    SdkUiType(HashMap<Target, String> value){
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
        return null;
    }

    public static String[] getSdkUiTypes(SdkUiType[] sdkUiTypes, Target target) {
        String ret[] = new String[sdkUiTypes.length];
        for (int i = 0; i < sdkUiTypes.length; i++) {
            ret[i] = sdkUiTypes[i].getValue(target);
        }
        return ret;
    }
}