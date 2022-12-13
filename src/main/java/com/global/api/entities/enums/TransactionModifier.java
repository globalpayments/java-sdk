package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TransactionModifier implements IFlag {
    None,
    Incremental,
    Additional,
    Offline,
    LevelII,
    FraudDecline,
    ChipDecline,
    CashBack,
    Voucher,
    Secure3D,
    HostedRequest,
    Recurring,
    EncryptedMobile,
    Fallback,
    Level_III,
    DecryptedMobile,
    AlternativePaymentMethod,
    DeletePreAuth,
    BankPayment,
    Merchant;

    public long getLongValue() {
        return 1 << this.ordinal();
    }
    public static Set<TransactionModifier> getSet(long value) {
        EnumSet<TransactionModifier> flags = EnumSet.noneOf(TransactionModifier.class);
        for(TransactionModifier flag : TransactionModifier.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }
}
