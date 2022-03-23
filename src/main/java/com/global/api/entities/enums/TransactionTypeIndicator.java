package com.global.api.entities.enums;

public enum TransactionTypeIndicator implements IStringConstant{
    ActivateCancellation("CAN-ACT"),
    ActivateReversal("REV-ACT"),
    BalanceInquiry("BALINQRY"),
    CardActivation("ACTIVATE"),
    CardIssue("ISSUE"),
    IssueCancellation("CAN-ISS"),
    IssueReversal("REV-ISS"),
    MerchandiseReturn("RETURN"),
    MerchandiseReturnReversal("REV-RTRN"),
    PreAuthorization("PRE-AUTH"),
    PreAuthorizationCompletion("PRE-COMP"),
    PreAuthorizationReversal("REV-PRE"),
    Purchase("PURCHASE"),
    PurchaseCancellation("CAN-PRCH"),
    PurchaseReversal("REV-PRCH"),
    RechargeCardBalance("RECHARGE"),
    RechargeReversal("REV-RCHG");

    final private String value;

    TransactionTypeIndicator(String value) {
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
