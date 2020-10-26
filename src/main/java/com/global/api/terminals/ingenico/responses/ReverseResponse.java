package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.ReverseStatus;

public class ReverseResponse extends IngenicoTerminalResponse implements IDeviceResponse {
	private byte[] _buffer;
	
	public ReverseResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		_buffer = buffer;
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		if (response.length > 0) {
			super.parseResponse(response);
			String rawData = new String(response, StandardCharsets.UTF_8);
			setStatus(ReverseStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());	
		}
	}
	
	@Override
	public String toString() {
		return new String(_buffer, StandardCharsets.UTF_8);
	}
}
