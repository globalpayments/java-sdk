package com.global.api.terminals.ingenico.variables;

public class INGENICO_REQ_CMD {
	public final String CANCEL = "CMD=CANCEL";
	public final String DUPLICATE = "CMD=DUPLIC";
	public final String REVERSE = "CMD=REVERSE";
	public final String REVERSE_WITH_ID = "CMD=REV%s";
	public final String REPORT = "0100000001100826EXT0100000A010B010CMD=%s";
	public final String RECEIPT = "0100000001100826EXT0100000A010B010CMD=%s";
	public final String REQUEST_MESSAGE = "0100000001100826EXT0100000A010B010";
}
