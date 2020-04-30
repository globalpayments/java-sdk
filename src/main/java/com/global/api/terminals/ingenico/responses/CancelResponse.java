package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.ingenico.variables.CancelStatus;

public class CancelResponse extends IngenicoTerminalResponse implements IDeviceResponse {

	public CancelResponse(byte[] buffer) {
		super(buffer);
		parseResponse(buffer);
	}

	@Override
	public void parseResponse(byte[] response) {
        super.parseResponse(response);
        rawData = new String(response, StandardCharsets.UTF_8);
        setStatus(CancelStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3))).toString());
    }
}
