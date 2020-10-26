package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum PATRequestType {
	TABLE_LOCK(1),
	TABLE_UNLOCK(2),
	RECEIPT_MESSAGE(3),
	TABLE_LIST(4),
	TRANSACTION_OUTCOME(5),
	ADDITIONAL_MESSAGE(6),
	TRANSFER_DATA(7),
	SPLITSALE_REPORT(8),
	TICKET(9),
	EOD_REPORT(10);
	
	private final static Map map = new HashMap<Object, Object>();
	private final Integer type;

	PATRequestType(Integer type) {
		this.type = type;
	}

	static {
		for (PATRequestType _type : PATRequestType.values())
			map.put(_type.type, _type);
	}

	public static PATRequestType getEnumName(Integer val) {
		return (PATRequestType) map.get(val);
	}

	public Integer getValue() {
		return type;
	}
}
