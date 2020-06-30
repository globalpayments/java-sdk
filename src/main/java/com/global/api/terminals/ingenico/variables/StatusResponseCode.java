package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum StatusResponseCode {
	Status(83), AppVersion(86), HandsetNumber(72), TerminalId(84);
	
	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	StatusResponseCode(int status) {
		this.status = status;
	}

	static {
		for (StatusResponseCode _status : StatusResponseCode.values())
			map.put(_status.status, _status);
	}

	public static StatusResponseCode getEnumName(int val) {
		return (StatusResponseCode) map.get(val);
	}

	public int getStatusResponseCode() {
		return this.status;
	}
}
