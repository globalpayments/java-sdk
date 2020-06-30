package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.ParseFormat;
import com.global.api.terminals.ingenico.variables.TerminalConfigStatus;

public class TerminalConfigResponse extends IngenicoTerminalResponse implements IDeviceResponse {

	public TerminalConfigResponse(byte[] buffer) {
		super(buffer, ParseFormat.Transaction);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
        super.parseResponse(response);
        rawData = new String(response, StandardCharsets.UTF_8);
        setStatus(TerminalConfigStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
    }

}
