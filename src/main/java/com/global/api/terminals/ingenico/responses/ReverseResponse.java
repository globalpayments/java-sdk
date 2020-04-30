package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.ReverseStatus;

public class ReverseResponse extends IngenicoTerminalResponse implements IDeviceResponse {

	public ReverseResponse(byte[] buffer) {
		super(buffer);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
		rawData = new String(response, StandardCharsets.UTF_8);
		setStatus(ReverseStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
	}
}
