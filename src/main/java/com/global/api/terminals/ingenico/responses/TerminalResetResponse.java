package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.TerminalResetStatus;

public class TerminalResetResponse extends IngenicoTerminalResponse implements IDeviceResponse {

	public TerminalResetResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
        rawData = new String(response, StandardCharsets.UTF_8);
        setStatus(TerminalResetStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
	}
}
