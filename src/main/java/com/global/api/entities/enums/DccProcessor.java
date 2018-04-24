package com.global.api.entities.enums;

public enum DccProcessor implements IStringConstant {	
	Fexco("fexco"),
	Euroconex("euroconex");
	
    String value;
    DccProcessor(String value) {
        this.value = value;
    }
    public String getValue() { return value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
