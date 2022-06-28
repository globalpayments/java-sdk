package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardPresentIndicator implements IStringConstant {
    CardPresent("0"),
    CardNotPresent("1");
    String value;
    CardPresentIndicator(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
