package com.global.api.entities.enums;

public enum SafReportSummary implements IStringConstant {
	NEWLY_STORED_RECORD_REPORT("0"),
	FAILED_RECORD_REPORT("1"),
	ALL_REPORT("2");
	
	String value;
	SafReportSummary(String value) { this.value = value; }
	public byte[] getBytes() { return value.getBytes(); }
	public String getValue() { return value; }
}