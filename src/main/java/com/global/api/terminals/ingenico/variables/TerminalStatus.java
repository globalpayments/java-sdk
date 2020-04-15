package com.global.api.terminals.ingenico.variables;

public enum TerminalStatus {
	STANDARD_MODE (0),
	VENDING_MODE (1);
	
	private final int mode;
	TerminalStatus(int mode) { this.mode = mode; }
	public int getTerminalStatus() {
		return this.mode;
	}
}
