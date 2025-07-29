package com.global.api.entities;

import com.global.api.entities.enums.IStringConstant;

public enum BankList implements IStringConstant {
    PKOBANKPOLSKISA("pkobankpolskisa"),
    SANTANDER("santander"),
    ING("ing"),
    BANKPEKAOSA("bankpekaosa"),
    MBANK("mbank"),
    ALIOR("alior"),
    BNPPARIBAS("bnpparibas"),
    MILLENIUM("millenium"),
    CREDITAGRICOLE("creditagricole"),
    CITI("citi"),
    INTELIGO("inteligo"),
    BANKISPOLDZIELCZE("bankispoldzielcze"),
    BOSBANK("bosbank"),
    NESTBANK("nestbank"),
    VELOBANK("velobank"),
    BANKNOWYSA("banknowysa"),
    PLUSBANK("plusbank"),
    BANKPOCZTOWY("bankpocztowy");

    private final String value;

    BankList(String value) {
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    public String getValue() {
        return value;
    }
}