package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.ingenico.variables.POSIdentifier;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class POSIdentifierResponse extends IngenicoTerminalResponse implements IInitializeResponse {
	private byte[] _buffer;
	private String _serialNumber;

	public POSIdentifierResponse(byte[] buffer) {
		super(buffer, ParseFormat.PID);
		_buffer = buffer;
		parseResponse(buffer);
	}
	
	public String getSerialNumber() {
		return _serialNumber;
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
		String rawData = new String(response, StandardCharsets.UTF_8);
		_serialNumber = rawData.substring(12, 67).trim();
        setStatus(POSIdentifier.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
	}
	
	@Override
	public String toString() {
		return new String(_buffer, StandardCharsets.UTF_8);
	}
}