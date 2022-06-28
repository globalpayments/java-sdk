package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardHolderIDMethod implements IStringConstant {
    Unknown("0"),
    Signature("1"),
    PIN("2"),
    UnattendedTerminal("3"),
    MailOrTelephoneOrder("4");
    String value;
    CardHolderIDMethod(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
