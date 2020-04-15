package com.global.api.terminals.ingenico.variables;

import java.util.*;

public enum PaymentMode {
	APPLICATION(0), 
	MAILORDER(1);

	private final static Map map = new HashMap<Object, Object>();
	private final int mode;

	PaymentMode(int mode) {
		this.mode = mode;
	}

	static {
		for (PaymentMode _mode : PaymentMode.values())
			map.put(_mode.mode, _mode);
	}

	public static PaymentMode getEnumName(int val) {
		return (PaymentMode) map.get(val);
	}

	public int getPaymentMode() {
		return this.mode;
	}
}
