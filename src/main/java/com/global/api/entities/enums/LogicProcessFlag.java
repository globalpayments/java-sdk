package com.global.api.entities.enums;

public enum LogicProcessFlag implements IStringConstant{
    Space(" "),
    NotCapable("N"),
    Capable("P");
	
	String value;
	LogicProcessFlag(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
