package com.example.restservice.entities.getAuthenticationData;

public class GetAuthenticationDataOutput {

	String status;
	String liabilityShift;
	String serverTransactionId;

	public GetAuthenticationDataOutput() {

	}
	
	public String getStatus() {
		return status;
	}

	public GetAuthenticationDataOutput setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getLiabilityShift() {
		return liabilityShift;
	}

	public GetAuthenticationDataOutput setLiabilityShift(String liabilityShift) {
		this.liabilityShift = liabilityShift;
		return this;
	}

	public String getServerTransactionId() {
		return serverTransactionId;
	}

	public GetAuthenticationDataOutput setServerTransactionId(String serverTransactionId) {
		this.serverTransactionId = serverTransactionId;
		return this;
	}

}