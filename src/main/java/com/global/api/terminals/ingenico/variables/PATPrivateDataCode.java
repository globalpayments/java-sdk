package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum PATPrivateDataCode {
	WaiterId(79),
	TableId(76),
	TerminalId(84),
	TerminalCurrency(67);
	
	private final static Map map = new HashMap<Object, Object>();
	private final int code;

	PATPrivateDataCode(int code) {
		this.code = code;
	}

	static {
		for (PATPrivateDataCode _code : PATPrivateDataCode.values())
			map.put(_code.code, _code);
	}

	public static PATPrivateDataCode getEnumName(Integer val) {
		return (PATPrivateDataCode) map.get(val);
	}

	public int getValue() {
		return this.code;
	}
}
