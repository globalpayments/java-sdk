package com.global.api.entities.enums;

public enum SafUpload implements IStringConstant {
	NEWLY_STORED_TRANSACTION("0"),
	FAILED_TRANSACTION("1"),
	ALL_TRANSACTION("2");
	
	String value;
	SafUpload(String value) { this.value = value; }
	public byte[] getBytes() { return value.getBytes(); }
	public String getValue() { return value; }
}
