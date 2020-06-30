package com.global.api.terminals.ingenico.variables;

import java.util.HashMap;
import java.util.Map;

public enum TerminalState {
	SUCCESS(0), FAILED(7);
	
	private final int state;
	private final static Map map = new HashMap<Object, Object>();

	TerminalState(int state) {
		this.state = state;
	}

	static {
		for (TerminalState _state : TerminalState.values())
			map.put(_state.state, _state);
	}

	public static TerminalState getEnumName(int val) {
		return (TerminalState) map.get(val);
	}

	public int getTerminalState() {
		return this.state;
	}
}
