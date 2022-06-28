package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.INumericConstant;
import com.global.api.entities.enums.IStringConstant;

public enum ProcessingFlag2 implements IStringConstant {
    NonEMVDevice("0"),
    EMVCapableDevice("5");

    String  value;
    ProcessingFlag2(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
