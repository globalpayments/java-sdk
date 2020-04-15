package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum CancelStatus {
	CANCEL_DONE(9), CANCEL_FAILED(7);

	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	CancelStatus(int status) {
		this.status = status;
	}

	static {
		for (CancelStatus _status : CancelStatus.values())
			map.put(_status.status, _status);
	}

	public static CancelStatus getEnumName(int val) {
		return (CancelStatus) map.get(val);
	}

	public int getCancelStatus() {
		return this.status;
	}
}
