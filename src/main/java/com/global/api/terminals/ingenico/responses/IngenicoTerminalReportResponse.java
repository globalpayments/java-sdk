package com.global.api.terminals.ingenico.responses;

import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class IngenicoTerminalReportResponse extends IngenicoTerminalResponse implements ITerminalReport {

	public IngenicoTerminalReportResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		super.parseResponse(buffer);
	}
}
