package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.ingenico.variables.POSIdentifier;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class POSIdentifierResponse extends IngenicoTerminalResponse implements IInitializeResponse {

	public POSIdentifierResponse(byte[] buffer) {
		super(buffer, ParseFormat.PID);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
		super.parseResponse(response);
		rawData = new String(response, StandardCharsets.UTF_8);
		serialNumber = rawData.substring(12, 67).trim();
        setStatus(POSIdentifier.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
	}
}