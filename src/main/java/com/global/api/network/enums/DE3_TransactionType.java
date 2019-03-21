package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE3_TransactionType implements IStringConstant {
    GoodsAndService("00"),
    Cash("01"),
    Debit_Adjustment("02"),
    CheckGuarantee("03"),
    CheckVerification("04"),
    GoodsAndServiceWithCashDisbursement("09"),
    AccountFunding_NonCashFinancialInstrument("10"),
    QuasiCashAndScrip("11"),
    CashSale("17"),
    Debit_FrequencyBenefit("18"),
    Return("20"),
    Deposit("21"),
    Credit_Adjustment("22"),
    CheckDepositGuarantee("23"),
    CheckDeposit("24"),
    Credit_FrequencyBenefit("28"),
    AvailableFundsInquiry("30"),
    BalanceInquiry("31"),
    LedgerBalanceInquiry("32"),
    AddressOrAccountVerification("33"),
    MiscInquiryVerification("38"),
    CardHolderAccountsTransfer("40"),
    TransferBetweenCardholders("48"),
    Payment("50"),
    LoadValue("60"),
    UnloadValue("61"),
    Activate("90"),
    Application("91"),
    CashCard_Adjustment("92"),
    Activate_PreValuedCard("93");

    private final String value;
    DE3_TransactionType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
