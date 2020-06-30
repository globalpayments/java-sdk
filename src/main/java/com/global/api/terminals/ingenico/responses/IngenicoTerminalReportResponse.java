package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class IngenicoTerminalReportResponse extends IngenicoTerminalResponse implements ITerminalReport {

	public IngenicoTerminalReportResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
		rawData = new String(response, StandardCharsets.UTF_8);
		privateData = rawData.substring(70, rawData.length());
	}
}
