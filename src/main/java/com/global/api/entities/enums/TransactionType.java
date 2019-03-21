package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TransactionType implements IFlag {
    Decline,
    Verify,
    Capture,
    Auth,
    Refund,
    Reversal,
    Sale,
    Edit,
    Void,
    AddValue,
    Balance,
    Activate,
    Alias,
    Replace,
    Reward,
    Deactivate,
    BatchClose,
    Create,
    Delete,
    BenefitWithdrawal,
    Fetch,
    Search,
    Hold,
    Release,
    VerifyEnrolled,
    VerifySignature,
    DccRateLookup,
    Increment,
    CashOut,
    DataCollect;

    public long getLongValue() {
        return 1 << this.ordinal();
    }

    public static Set<TransactionType> getSet(long value) {
        EnumSet<TransactionType> flags = EnumSet.noneOf(TransactionType.class);
        for(TransactionType flag : TransactionType.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }
}
