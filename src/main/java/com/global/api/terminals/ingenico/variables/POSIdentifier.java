package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum POSIdentifier {
	SUCCESS(0), FAILED(7);

	private final int id;
	private final static Map map = new HashMap<Object, Object>();

	POSIdentifier(int id) {
		this.id = id;
	}

	static {
		for (POSIdentifier _id : POSIdentifier.values())
			map.put(_id.id, _id);
	}

	public static POSIdentifier getEnumName(int val) {
		return (POSIdentifier) map.get(val);
	}

	public int getPOSIdentifier() {
		return this.id;
	}
}
