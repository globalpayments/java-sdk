package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum ReverseStatus {
	REVERSAL_SUCCES(0), REVERSAL_FAILED(7), NOTHING_TO_REVERSE(9);

	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	ReverseStatus(int status) {
		this.status = status;
	}

	public int getReverseStatus() {
		return this.status;
	}

	static {
		for (ReverseStatus _status : ReverseStatus.values())
			map.put(_status.status, _status);
	}

	public static ReverseStatus getEnumName(int val) {
		return (ReverseStatus) map.get(val);
	}
}
