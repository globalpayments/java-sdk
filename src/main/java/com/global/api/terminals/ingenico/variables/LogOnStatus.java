package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum LogOnStatus {
	SUCCESS(9), FAILED(7);

	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	LogOnStatus(int status) {
		this.status = status;
	}

	static {
		for (LogOnStatus _status : LogOnStatus.values())
			map.put(_status.status, _status);
	}

	public static LogOnStatus getEnumName(int val) {
		return (LogOnStatus) map.get(val);
	}

	public int getLogOnStatus() {
		return this.status;
	}
}
