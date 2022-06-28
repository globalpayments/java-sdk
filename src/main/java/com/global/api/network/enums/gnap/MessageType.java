package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum MessageType implements IStringConstant {
    FinancialTransactions("F"),
    AdministrativeTransactions("A");

    String value;
    MessageType(String value) { this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
