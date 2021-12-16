package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentMethodType implements IFlag {
    Reference,
    Credit,
    Debit,
    EBT,
    Cash,
    ACH,
    Gift,
    Recurring,
    Other,
    APM,
    Ewic;

    public long getLongValue() {
        return 1 << this.ordinal();
    }
    public static Set<PaymentMethodType> getSet(long value) {
        EnumSet<PaymentMethodType> flags = EnumSet.noneOf(PaymentMethodType.class);
        for(PaymentMethodType flag : PaymentMethodType.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }
}
