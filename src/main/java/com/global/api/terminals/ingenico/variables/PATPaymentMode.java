package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum PATPaymentMode {
	NO_ADDITIONAL_MSG(0),
	USE_ADDITIONAL_MSG(1);

	private final static Map map = new HashMap<Object, Object>();
	private final int mode;

	PATPaymentMode(int mode) {
		this.mode = mode;
	}

	static {
		for (PATPaymentMode _mode : PATPaymentMode.values())
			map.put(_mode.mode, _mode);
	}

	public static PATPaymentMode getEnumName(Integer val) {
		return (PATPaymentMode) map.get(val);
	}

	public int getValue() {
		return this.mode;
	}
}
