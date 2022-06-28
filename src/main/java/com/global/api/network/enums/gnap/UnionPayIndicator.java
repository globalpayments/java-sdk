package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum UnionPayIndicator implements IStringConstant {
    NonUnionPayTransaction("0"),
    UnionPayTransaction("1");

    String value;
    UnionPayIndicator(String value){ this.value=value;}
    public String getValue(){ return this.value;}
    public byte[] getBytes() { return this.value.getBytes(); }

}