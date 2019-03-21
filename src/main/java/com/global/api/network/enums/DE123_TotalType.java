package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE123_TotalType implements IStringConstant {
    NotSpecific("   "),
    AccountLedgerBalance("001"),
    AccountAvailableBalance("002"),
    AmountOwing("003"),
    AmountDue("004"),
    AccountAvailableCredit("005"),
    AmountCash("040"),
    AmountGoodsAndServices("041"),
    AmountTax("056"),
    AmountDiscount("057"),
    AmountFundsForDeposit("058"),
    TransactionFee("100"),
    PointBalance("200"),
    AwardBalance("201");

    private final String value;
    DE123_TotalType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
