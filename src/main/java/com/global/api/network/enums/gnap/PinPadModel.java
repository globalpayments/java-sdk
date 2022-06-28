package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum PinPadModel implements IStringConstant {
    InternalPINPad(""),
    SC5000("5000"),
    PINPad810("810P"),
    PINPadWithContactless810("810C"),
    PINPad820("820P");

    String value;
    PinPadModel(String value) { this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
