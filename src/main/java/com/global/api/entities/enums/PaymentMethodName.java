package com.global.api.entities.enums;

import java.util.HashMap;

public enum PaymentMethodName implements IMappedConstant {
    APM(new HashMap<Target, String>() {{
        put(Target.GP_API, "APM");
    }}),

    DigitalWallet(new HashMap<Target, String>() {{
        put(Target.GP_API, "DIGITAL WALLET");
    }}),

    Card(new HashMap<Target, String>() {{
        put(Target.GP_API, "CARD");
    }}),

    BankTransfer(new HashMap<Target, String>() {{
        put(Target.GP_API, "BANK TRANSFER");
    }}),

    BankPayment(new HashMap<Target, String>() {{
        put(Target.Realex, "BANK PAYMENT");
        put(Target.GP_API, "BANK PAYMENT");
    }}),

    BNPL(new HashMap<Target, String>() {{
        put(Target.GP_API, "BNPL");
    }});

    HashMap<Target, String> value;
    PaymentMethodName(HashMap<Target, String> value){
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

    public static PaymentMethodName fromString(String value, Target target) {
        for (PaymentMethodName currentEnum : PaymentMethodName.values()) {
            if (currentEnum.getValue(target).equalsIgnoreCase(value)) {
                return currentEnum;
            }
        }
        return null;
    }

}