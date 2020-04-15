package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum PaymentType {
	SALE (0),
	REFUND (1),
	COMPLETION (2),
	PREAUTH (3),
	TAXFREE_CREDIT_REFUND (4),
	TAXFREE_CASH_REFUND (5),
	ACCOUNT_VERIFICATION (6),
	REFERRAL_CONFIRMATION (9);
	
	private final static Map map = new HashMap<Object, Object>();
	private final int type;
	
	PaymentType(int type) { this.type = type; } 
	
	public Integer getValue() {
		return this.type;
	}
	
	public static PaymentType getEnumName(Integer val) {
		return (PaymentType) map.get(val);
	}
}
