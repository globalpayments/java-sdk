package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE123_TransactionType implements IStringConstant {
    Debits("000"),
    DebitReversals("001"),
    DebitLessReversals("002"),
    Credits("005"),
    CreditReversals("006"),
    CreditLessReversals("007"),
    Inquiry("010"),
    GoodsAndService("100"),
    Cash("101"),
    CheckGuarantee("103"),
    CheckVerification("104"),
    GoodsAndService_CashDisbursement("109"),
    ElectronicBenefitsTransfer_Debit("114"),
    Return("120"),
    Deposit("121"),
    Adjustment("122"),
    ElectronicBenefitsTransfer_Credit("127"),
    AvailableFundsInquiry("130"),
    BalanceInquiry("131"),
    LedgerBalanceInquiry("132"),
    AddressVerification("133"),
    ElectronicBenefitsTransfer_Inquiry("136"),
    Payment("150"),
    LoadValue("160"),
    UnloadValue("161"),
    Activate("190"),
    //Adjustment("192"),
    AllVoids_Reversals("298"),
    AllVoids_Voids("299");

    private final String value;
    DE123_TransactionType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
