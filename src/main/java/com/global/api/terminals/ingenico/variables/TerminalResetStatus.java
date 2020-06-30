package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum TerminalResetStatus {
	SUCCESS(9), FAILED(7);

	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	TerminalResetStatus(int status) {
		this.status = status;
	}

	static {
		for (TerminalResetStatus _status : TerminalResetStatus.values())
			map.put(_status.status, _status);
	}

	public static TerminalResetStatus getEnumName(int val) {
		return (TerminalResetStatus) map.get(val);
	}

	public int getTerminalResetStatus() {
		return this.status;
	}
}
