package com.global.api.terminals.ingenico.variables;

import java.util.*;

public enum TransactionStatus {
	SUCCESS(0), 
	REFERRAL(2), 
	CANCELLED_BY_USER(6), 
	FAILED(7), 
	RECEIVED(9);

	private final int status;
	private final static Map map = new HashMap<Object, Object>();

	TransactionStatus(int status) {
		this.status = status;
	}

	static {
		for (TransactionStatus _status : TransactionStatus.values())
			map.put(_status.status, _status);
	}

	public static TransactionStatus getEnumName(int val) {
		return (TransactionStatus) map.get(val);
	}

	public int getTransactionStatus() {
		return this.status;
	}
}
