package com.global.api.entities.reporting;

import java.util.Date;
import java.util.List;

public class AltPaymentData {
	private String status;
	private String statusMessage;
	private String buyerEmailAddress;
	private Date stateDate;
	private List<AltPaymentProcessorInfo> processorResponseInfo;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getBuyerEmailAddress() {
		return buyerEmailAddress;
	}
	public void setBuyerEmailAddress(String buyerEmailAddress) {
		this.buyerEmailAddress = buyerEmailAddress;
	}
	public Date getStateDate() {
		return stateDate;
	}
	public void setStateDate(Date stateDate) {
		this.stateDate = stateDate;
	}
	public List<AltPaymentProcessorInfo> getProcessorResponseInfo() {
		return processorResponseInfo;
	}
	public void setProcessorResponseInfo(List<AltPaymentProcessorInfo> processorResponseInfo) {
		this.processorResponseInfo = processorResponseInfo;
	}
}