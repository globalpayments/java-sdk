package com.global.api.entities.reporting;

public class CheckData {
	private String accountInfo;
	private String consumerInfo;
	private String checkAction;
	private String checkType;
	private String dataEntryMode;
	private String secCode;
	
	public String getAccountInfo() {
		return accountInfo;
	}
	public void setAccountInfo(String accountInfo) {
		this.accountInfo = accountInfo;
	}
	public String getConsumerInfo() {
		return consumerInfo;
	}
	public void setConsumerInfo(String consumerInfo) {
		this.consumerInfo = consumerInfo;
	}
	public String getCheckAction() {
		return checkAction;
	}
	public void setCheckAction(String checkAction) {
		this.checkAction = checkAction;
	}
	public String getCheckType() {
		return checkType;
	}
	public void setCheckType(String checkType) {
		this.checkType = checkType;
	}
	public String getDataEntryMode() {
		return dataEntryMode;
	}
	public void setDataEntryMode(String dataEntryMode) {
		this.dataEntryMode = dataEntryMode;
	}
	public String getSecCode() {
		return secCode;
	}
	public void setSecCode(String secCode) {
		this.secCode = secCode;
	}
}