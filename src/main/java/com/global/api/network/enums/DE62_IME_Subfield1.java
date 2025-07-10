package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE62_IME_Subfield1 implements IStringConstant {

    Val_210("210"),
    Val_211("211"),
    Val_212("212"),
    Val_214("214"),
    Val_216("216"),
    Val_217("217"),
    Val_242("242"),
    Val_246("246"),
    Val_247("247");

    private final String value;
    DE62_IME_Subfield1(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}