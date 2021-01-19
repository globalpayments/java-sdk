package com.global.api.entities.enums;

import java.util.HashMap;

public enum StoredCredentialType implements IMappedConstant {
    OneOff(new HashMap<Target, String>() {{
        put(Target.Realex, "oneoff");
    }}),
    Installment(new HashMap<Target, String>() {{
        put(Target.Realex, "installment");
        put(Target.GP_API, "INSTALLMENT");
    }}),
    Recurring(new HashMap<Target, String>() {{
        put(Target.Realex, "recurring");
        put(Target.GP_API, "RECURRING");
    }}),
    Unscheduled(new HashMap<Target, String>() {{
        put(Target.GP_API, "UNSCHEDULED");
    }}),
    Subscription(new HashMap<Target, String>() {{
        put(Target.GP_API, "SUBSCRIPTION");
    }}),
    MaintainPaymentMethod(new HashMap<Target, String>() {{
        put(Target.GP_API, "MAINTAIN_PAYMENT_METHOD");
    }}),
    MaintainPaymentVerification(new HashMap<Target, String>() {{
        put(Target.GP_API, "MAINTAIN_PAYMENT_VERIFICATION");
    }});

    HashMap<Target, String> value;
    StoredCredentialType(HashMap<Target, String> value){
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
