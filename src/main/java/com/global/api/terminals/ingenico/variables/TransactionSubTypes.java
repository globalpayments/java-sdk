package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

import com.global.api.entities.enums.IByteConstant;

public enum TransactionSubTypes implements IByteConstant {
	SPLIT_SALE_TXN(0x53), 
	DCC_TXN(0x44), 
	REFERRAL_RESULT(0x82);

	private final byte code;
	private final static Map map = new HashMap<Object, Object>();

	TransactionSubTypes(int code) {
		this.code = (byte) code;
	}

	public byte getByte() {
		return this.code;
	}

	static {
		for (TransactionSubTypes _code : TransactionSubTypes.values())
			map.put(_code.code, _code);
	}

	public static TransactionSubTypes getEnumName(int val) {
		return (TransactionSubTypes) map.get(val);
	}

	@Override
	public String toString() {
		String rvalue = super.toString();
		return String.format("[%s]", rvalue);
	}
}
