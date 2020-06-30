package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum RepFieldCode {
	Authcode(67),
	CashbackAmount(90),
	GratuityAmount(89),
	FinalTransactionAmount(77),
	AvailableAmount(65),
	DccCurrency(85),
	DccConvertedAmount(79),
	PaymentMethod(80),
	TransactionSubType(84),
	SplitSalePaidAmount(83),
	DccOperationStatus(68);
	
	private final int code;
	private final static Map map = new HashMap<Object, Object>();

	RepFieldCode(int code) {
		this.code = code;
	}

	static {
		for (RepFieldCode _code : RepFieldCode.values())
			map.put(_code.code, _code);
	}

	public static RepFieldCode getEnumName(int val) {
		return (RepFieldCode) map.get(val);
	}

	public int getRepFieldCode() {
		return this.code;
	}
}
