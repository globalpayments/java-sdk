package com.global.api.entities.enums;

public enum OrderTransactionType implements IStringConstant {
    GoodsAndServicesPurchase("GOODS_SERVICE_PURCHASE"),
    CheckAcceptance("CHECK_ACCEPTANCE"),
    AccountFunding("ACCOUNT_FUNDING"),
    QuasiCashTransaction("QUASI_CASH_TRANSACTION"),
    PrepaidActivationAndLoad("PREPAID_ACTIVATION_AND_LOAD");

    String value;
    OrderTransactionType(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
