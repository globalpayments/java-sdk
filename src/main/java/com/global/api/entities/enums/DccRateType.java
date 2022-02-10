package com.global.api.entities.enums;

public enum DccRateType  implements IStringConstant {
    None("None"),
	Sale("S"),
	Refund("R");
	
    String value;
    DccRateType(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return this.value.getBytes(); }

}
