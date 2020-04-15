package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum TaxFreeType {
	CREDIT (4),
	CASH (5);
	
	private final static Map map = new HashMap<Object, Object>();
	private final int type;
	
	TaxFreeType(int type) { this.type = type; } 
	
	public int toInteger() {
		return this.type;
	}
	
	public static TaxFreeType getEnumName(Integer val) {
		return (TaxFreeType) map.get(val);
	}
}
