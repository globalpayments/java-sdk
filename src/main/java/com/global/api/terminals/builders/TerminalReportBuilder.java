package com.global.api.terminals.builders;

import com.global.api.terminals.abstractions.*;
import com.global.api.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.*;
import com.global.api.terminals.pax.enums.PaxSearchCriteria;
import com.global.api.terminals.ingenico.variables.*;

public class TerminalReportBuilder {
	private TerminalReportType reportType;
	private ReceiptType receiptType;
	private ReportTypes type;
	private TerminalSearchBuilder _searchBuilder;

	public TerminalReportType getReportType() {
		return reportType;
	}

	public void setReportType(TerminalReportType reportType) {
		this.reportType = reportType;
	}

	public ReceiptType getReceiptType() {
		return receiptType;
	}

	public void setReceiptType(ReceiptType receiptType) {
		this.receiptType = receiptType;
	}
	
	public ReportTypes getType() {
		return type;
	}
	
	public void setType(ReportTypes type) {
		this.type = type;
	}

	public TerminalSearchBuilder getSearchBuilder() {
		if (_searchBuilder == null) {
			_searchBuilder = new TerminalSearchBuilder(this);
		}
		return _searchBuilder;

	}

	public TerminalReportBuilder(TerminalReportType reportType) {
		this.reportType = reportType;
	}

	public TerminalReportBuilder(ReceiptType receiptType) {
		this.receiptType = receiptType;
	}

	public TerminalReportBuilder(ReportTypes type) {
		this.type = type;
	}

	public <T> TerminalSearchBuilder where(PaxSearchCriteria criteria, T value) {
		return getSearchBuilder().And(criteria, value);
	}

	public ITerminalReport execute() throws ApiException {
		return execute("default");
	}

	public ITerminalReport execute(String configName) throws ApiException {
		DeviceController device = ServicesContainer.getInstance().getDeviceController(configName);
		return device.processReport(this);
	}
}
