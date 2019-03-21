package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE54_AccountType implements IStringConstant {
    Unspecified("00"),
    AccountLedgerBalance("01"),
    AccountAvailableBalance("02"),
    AmountOwing("03"),
    AmountDue("04"),
    AccountAvailableCredit("05"),
    AccountMaximumLimit("17"),
    AmountRemainingThisCycle("20");

    private final String value;
    DE54_AccountType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
