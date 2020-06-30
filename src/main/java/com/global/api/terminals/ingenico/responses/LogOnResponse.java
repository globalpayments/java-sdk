package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.LogOnStatus;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class LogOnResponse extends IngenicoTerminalResponse implements IDeviceResponse {

	public LogOnResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
		rawData = new String(response, StandardCharsets.UTF_8);
        setStatus(LogOnStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
	}
}
