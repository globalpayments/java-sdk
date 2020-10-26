package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum ParseFormat {
	Transaction(0), State(1), PID(2), PayAtTable(3), XML(4);
	
	private final int format;
	private final static Map map = new HashMap<Object, Object>();

	ParseFormat(int format) {
		this.format = format;
	}

	static {
		for (ParseFormat _format : ParseFormat.values())
			map.put(_format.format, _format);
	}

	public static ParseFormat getEnumName(int val) {
		return (ParseFormat) map.get(val);
	}

	public int getParseFormat() {
		return this.format;
	}
}
