package com.global.api.terminals.ingenico.responses;

import java.nio.charset.StandardCharsets;

import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.ingenico.variables.ParseFormat;

public class IngenicoTerminalReceiptResponse extends IngenicoBaseResponse implements ITerminalReport {
	
	public IngenicoTerminalReceiptResponse(byte[] buffer) {
		super(buffer, ParseFormat.XML);
		_buffer = buffer;
		
		String status = _buffer.length > 0 ? "SUCCESS" : "FAILED";
		setStatus(status);
	}
	
	@Override
	public String toString() {
		return new String(_buffer, StandardCharsets.ISO_8859_1);
	}
}
