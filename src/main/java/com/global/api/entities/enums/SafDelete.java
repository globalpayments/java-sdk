package com.global.api.entities.enums;

public enum SafDelete implements IStringConstant {
	NEWLY_STORED_TRANSACTION_RECORD("0"),
	FAILED_TRANSACTION_RECORD("1"),
	DELETE_ALL_SAF_RECORD("2");
	
	String value;
	SafDelete(String value) { this.value = value; }
	public byte[] getBytes() { return value.getBytes(); }
	public String getValue() { return value; }
}