package com.global.api.terminals.pax.responses;

import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.ITerminalReport;

public class PaxTerminalReport extends PaxBaseResponse implements ITerminalReport {
	public PaxTerminalReport(byte[] buffer, PaxMsgId[] messageIds) throws MessageException {
		super(buffer, messageIds);
	}

	public String getReportData() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setReportData(String reportData) {
		// TODO Auto-generated method stub
		
	}
}
