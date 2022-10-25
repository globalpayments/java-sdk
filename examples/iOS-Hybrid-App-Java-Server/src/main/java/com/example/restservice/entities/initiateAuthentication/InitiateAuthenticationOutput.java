package com.example.restservice.entities.initiateAuthentication;

public class InitiateAuthenticationOutput {

	String enrolled;
	String version;
	String status;
	String serverTransactionId;
	String dsTransferReference;
	String liabilityShift;
	String acsTransactionId;
	String acsReferenceNumber;
	String methodUrl;
	String payerAuthenticationRequest;
	String sessionDataFieldName;
	String messageVersion;
	String authenticationValue;
	String eci;
	String challengeMandated;
	String challenge;

	public InitiateAuthenticationOutput() {

	}

	public String getEnrolled() {
		return enrolled;
	}

	public InitiateAuthenticationOutput setEnrolled(String enrolled) {
		this.enrolled = enrolled;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public InitiateAuthenticationOutput setVersion(String version) {
		this.version = version;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public InitiateAuthenticationOutput setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getServerTransactionId() {
		return serverTransactionId;
	}

	public InitiateAuthenticationOutput setServerTransactionId(String serverTransactionId) {
		this.serverTransactionId = serverTransactionId;
		return this;
	}

	public String getDsTransferReference() {
		return dsTransferReference;
	}

	public InitiateAuthenticationOutput setDsTransferReference(String dsTransferReference) {
		this.dsTransferReference = dsTransferReference;
		return this;
	}

	public String getLiabilityShift() {
		return liabilityShift;
	}

	public InitiateAuthenticationOutput setLiabilityShift(String liabilityShift) {
		this.liabilityShift = liabilityShift;
		return this;
	}

	public String getAcsTransactionId() {
		return acsTransactionId;
	}

	public InitiateAuthenticationOutput setAcsTransactionId(String acsTransactionId) {
		this.acsTransactionId = acsTransactionId;
		return this;
	}

	public String getMethodUrl() {
		return methodUrl;
	}

	public InitiateAuthenticationOutput setMethodUrl(String methodUrl) {
		this.methodUrl = methodUrl;
		return this;
	}

	public String getAcsReferenceNumber() {
		return acsReferenceNumber;
	}

	public InitiateAuthenticationOutput setAcsReferenceNumber(String acsReferenceNumber) {
		this.acsReferenceNumber = acsReferenceNumber;
		return this;
	}

	public String getPayerAuthenticationRequest() {
		return payerAuthenticationRequest;
	}

	public InitiateAuthenticationOutput setPayerAuthenticationRequest(String payerAuthenticationRequest) {
		this.payerAuthenticationRequest = payerAuthenticationRequest;
		return this;
	}

	public String getSessionDataFieldName() {
		return sessionDataFieldName;
	}

	public InitiateAuthenticationOutput setSessionDataFieldName(String sessionDataFieldName) {
		this.sessionDataFieldName = sessionDataFieldName;
		return this;
	}

	public String getMessageVersion() {
		return messageVersion;
	}
	
	public InitiateAuthenticationOutput setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
		return this;
	}

	public String getAuthenticationValue() {
		return authenticationValue;
	}

	public InitiateAuthenticationOutput setAuthenticationValue(String authenticationValue) {
		this.authenticationValue = authenticationValue;
		return this;
	}

	public String getEci() {
		return eci;
	}

	public InitiateAuthenticationOutput setEci(String eci) {
		this.eci = eci;
		return this;
	}

	public String getChallengeMandated() {
		return challengeMandated;
	}

	public InitiateAuthenticationOutput setChallengeMandated(String challengeMandated) {
		this.challengeMandated = challengeMandated;
		return this;
	}

	public String getChallenge() {
		return challenge;
	}

	public InitiateAuthenticationOutput setChallenge(String challenge) {
		this.challenge = challenge;
		return this;
	}

}