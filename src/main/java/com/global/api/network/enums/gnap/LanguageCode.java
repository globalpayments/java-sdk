package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum LanguageCode implements IStringConstant {
    English("0"),
    French("1");

    String value;
    LanguageCode(String value){ this.value=value;}

    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    public String getValue(){ return this.value;}
}
