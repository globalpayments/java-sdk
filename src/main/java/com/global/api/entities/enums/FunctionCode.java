package com.global.api.entities.enums;

public enum FunctionCode implements IStringConstant{
	OfflineApprovedSaleAdvice("A"),
	BalanceInquiry("B"),
	OfflineDeclineAdvice("D"),
	Void("V"),
	ZERO_VALUE_ACCOUNT_VERIFFICATION("Z");

	
	String value;
	FunctionCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
