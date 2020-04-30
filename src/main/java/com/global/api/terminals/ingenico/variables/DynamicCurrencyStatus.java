package com.global.api.terminals.ingenico.variables;

import java.util.*;

public enum DynamicCurrencyStatus {
	CONVERSION_APPLIED (1),
	REJECTED (0);
	
	private final int status;
	private final static Map map = new HashMap<Object, Object>();
	DynamicCurrencyStatus(int status) { this.status = status; }
	public int getDynamicCurrencyStatus() {
		return this.status;
	}
	
	static {
		for (DynamicCurrencyStatus _status : DynamicCurrencyStatus.values())
			map.put(_status.status, _status);
	}

	public static DynamicCurrencyStatus getEnumName(int val) {
		return (DynamicCurrencyStatus) map.get(val);
	}
}
