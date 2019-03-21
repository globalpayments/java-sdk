package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE3_AccountType implements IStringConstant {
    Unspecified("00"),
    PinDebitAccount("08"),
    FleetAccount("09"),
    SavingsAccount("10"),
    CheckingAccount("20"),
    CreditAccount("30"),
    PurchaseAccount("38"),
    PrivateLabelAccount("39"),
    UniversalAccount("40"),
    InvestmentAccount("50"),
    CashCardAccount("60"),
    CashCard_CashAccount("65"),
    CashCard_CreditAccount("66"),
    FoodStampsAccount("80"),
    CashBenefitAccount("81"),
    LoyaltyAccount("90"),
    AchAccount("91"),
    eWIC("97");

    private final String value;
    DE3_AccountType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
