package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum Base24TransactionModifier implements IStringConstant {
    TelephoneAuthorizedPurchases("TA"),
    VoidTransactions("CR"),
    OtherTransaction("  ");

    String value;
    Base24TransactionModifier(String value){this.value=value;}

    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

    }
