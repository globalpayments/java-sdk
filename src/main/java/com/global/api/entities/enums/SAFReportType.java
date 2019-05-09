package com.global.api.entities.enums;

public enum SAFReportType implements IStringConstant {
	APPROVED("APPROVED SAF SUMMARY"),
	PENDING("PENDING SAF SUMMARY"),
	DECLINED("DECLINED SAF SUMMARY"),
	OFFLINE_APPROVED("OFFLINE APPROVED SAF SUMMARY"),
	PARTIALLY_APPROVED("PARTIALLY APPROVED  SAF SUMMARY"),
	APPROVED_VOID("APPROVED SAF VOID SUMMARY"),
	PENDING_VOID("PENDING SAF VOID SUMMARY"),
	DECLINED_VOID("DECLINED SAF VOID SUMMARY");
	
	String value;
	SAFReportType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}