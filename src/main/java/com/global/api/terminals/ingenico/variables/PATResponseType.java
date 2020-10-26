package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum PATResponseType {
	CONF_OK(1),
	CONF_NOK(0);
	
	private final static Map map = new HashMap<Object, Object>();
	private final Integer type;

	PATResponseType(Integer type) {
		this.type = type;
	}

	static {
		for (PATResponseType _type : PATResponseType.values())
			map.put(_type.type, _type);
	}

	public static PATResponseType getEnumName(Integer val) {
		return (PATResponseType) map.get(val);
	}

	public Integer getValue() {
		return type;
	}
}
