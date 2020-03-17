package com.global.api.entities.enums;

public enum SafMode implements IStringConstant {
	STAY_ONLINE("0"),
	STAY_OFFLINE("1"),
	OFFLINE_TILL_BATCH("2"),
	OFFLINE_ONDEMAND_OR_AUTO("3");
	
	String value;
	SafMode(String value) { this.value = value; }
	public byte[] getBytes() { return value.getBytes(); }
	public String getValue() { return value; }
}
