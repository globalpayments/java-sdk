package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum TerminalConfigStatus {
	SUCCESS(9), FAILED(7);
	
	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	TerminalConfigStatus(int status) {
		this.status = status;
	}

	static {
		for (TerminalConfigStatus _status : TerminalConfigStatus.values())
			map.put(_status.status, _status);
	}

	public static TerminalConfigStatus getEnumName(int val) {
		return (TerminalConfigStatus) map.get(val);
	}

	public int getTerminalConfigStatus() {
		return this.status;
	}
}
