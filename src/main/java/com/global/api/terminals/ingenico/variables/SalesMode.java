package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum SalesMode {
	STANDARD_SALE_MODE(0), VENDING_MODE(1);

	private final int mode;
	private final static Map map = new HashMap<Object, Object>();

	SalesMode(int mode) {
		this.mode = mode;
	}

	static {
		for (SalesMode _mode : SalesMode.values())
			map.put(_mode.mode, _mode);
	}

	public static SalesMode getEnumName(int val) {
		return (SalesMode) map.get(val);
	}

	public int getTerminalState() {
		return this.mode;
	}
}
