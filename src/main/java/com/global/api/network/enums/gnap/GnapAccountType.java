package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum GnapAccountType implements IStringConstant {
    DefaultAccountInteracAndUP("0"),
    ChequingAccount("1"),
    SavingsAccount("2");

    String value;
    GnapAccountType(String value){this.value=value;}
    public String getValue(){ return this.value;}
    public byte[] getBytes() { return this.value.getBytes(); }
}
