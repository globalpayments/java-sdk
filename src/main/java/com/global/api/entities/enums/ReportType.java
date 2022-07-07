package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ReportType implements IFlag {
    FindTransactions,
    Activity,
    BatchDetail,
    BatchHistory,
    BatchSummary,
    OpenAuths,
    Search,
    TransactionDetail,
    DepositDetail,
    DisputeDetail,
    SettlementDisputeDetail,
    FindTransactionsPaged,
    FindSettlementTransactionsPaged,
    FindDepositsPaged,
    FindDisputesPaged,
    FindSettlementDisputesPaged,
    StoredPaymentMethodDetail,
    FindStoredPaymentMethodsPaged,
    ActionDetail,
    FindActionsPaged,
    FindBankPayment,
    DocumentDisputeDetail;

    public long getLongValue() {
        return 1 << this.ordinal();
    }
    public static Set<ReportType> getSet(long value) {
        EnumSet<ReportType> flags = EnumSet.noneOf(ReportType.class);
        for(ReportType flag : ReportType.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }
}
