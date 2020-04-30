package com.global.api.terminals.ingenico.variables;

import java.util.*;

public enum PaymentMethod {
	KEYED (1),
	SWIPED (2),
	CHIP (3),
	CONTACTLESS (4);
	
	private final int method;
	private final static Map map = new HashMap<Object, Object>();
	PaymentMethod(int method) { this.method = method; }
	public int getPaymentMethod() {
		return this.method;
	}
	
	static {
		for (PaymentMethod _method : PaymentMethod.values())
			map.put(_method.method, _method);
	}

	public static PaymentMethod getEnumName(int val) {
		return (PaymentMethod) map.get(val);
	}
}
