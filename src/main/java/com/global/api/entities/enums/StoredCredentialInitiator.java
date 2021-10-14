package com.global.api.entities.enums;
import java.util.HashMap;

public enum StoredCredentialInitiator implements IMappedConstant {
    CardHolder(new HashMap<Target, String>() {{
        put(Target.Realex, "cardholder");
        put(Target.Portico, "C");
        put(Target.GP_API, "PAYER");
        put(Target.Genius, "UNSCHEDULEDCIT");
    }}),
    Merchant(new HashMap<Target, String>() {{
        put(Target.Realex, "merchant");
        put(Target.Portico, "M");
        put(Target.GP_API, "MERCHANT");
        put(Target.Genius, "UNSCHEDULEDMIT");
    }}),
    Scheduled(new HashMap<Target, String>() {{
        put(Target.Realex, "scheduled");
        put(Target.Genius, "RECURRING");
    }}),
    Installment(new HashMap<Target, String>() {{
        put(Target.Genius, "INSTALLMENT");
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
