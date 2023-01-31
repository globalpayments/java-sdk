package com.example.restservice.entities.checkEnrollment;

public class CheckEnrollmentOutput {

	String enrolled;
	String version;
	String messageVersion;
	String status;
	String liabilityShift;
	String serverTransactionId;
	String sessionDataFieldName;
	String methodUrl;
	String methodData;
	String messageType;
	String acsInfoIndicator;

	public CheckEnrollmentOutput() {

	}

	public String getStatus() {
		return status;
	}

	public String getServerTransactionId() {
		return serverTransactionId;
	}

	public String getMessageVersion() {
		return messageVersion;
	}

	public String getEnrolled() {
		return enrolled;
	}

	public String getVersion() {
		return version;
	}

	public String getLiabilityShift() {
		return liabilityShift;
	}

	public String getSessionDataFieldName() {
		return sessionDataFieldName;
	}

	
	public CheckEnrollmentOutput setStatus(String status) {
		this.status = status;
		return this;
	}

	public CheckEnrollmentOutput setServerTransactionId(String serverTransactionId) {
		this.serverTransactionId = serverTransactionId;
		return this;
	}

	public CheckEnrollmentOutput setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
		return this;
	}
	
	public CheckEnrollmentOutput setEnrolled(String enrolled) {
		this.enrolled = enrolled;
		return this;
	}

	public CheckEnrollmentOutput setVersion(String version) {
		this.version = version;
		return this;
	}
	
	public CheckEnrollmentOutput setLiabilityShift(String liabilityShift) {
		this.liabilityShift = liabilityShift;
		return this;
	}

	public CheckEnrollmentOutput setSessionDataFieldName(String sessionDataFieldName) {
		this.sessionDataFieldName = sessionDataFieldName;
		return this;
	}
	
	public String getMethodUrl() {
		return methodUrl;
	}

	public CheckEnrollmentOutput setMethodUrl(String methodUrl) {
		this.methodUrl = methodUrl;
		return this;
	}

	public String getMethodData() {
		return methodData;
	}

	public CheckEnrollmentOutput setMethodData(String methodData) {
		this.methodData = methodData;
		return this;
	}

	public String getMessageType() {
		return messageType;
	}

	public CheckEnrollmentOutput setMessageType(String messageType) {
		this.messageType = messageType;
		return this;
	}

	public String getAcsInfoIndicator() {
		return acsInfoIndicator;
	}

	public CheckEnrollmentOutput setAcsInfoIndicator(String acs_info_indicator) {
		this.acsInfoIndicator = acs_info_indicator;
		return this;
	}

}