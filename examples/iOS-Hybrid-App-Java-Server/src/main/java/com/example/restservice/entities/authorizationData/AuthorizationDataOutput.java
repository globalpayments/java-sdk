package com.example.restservice.entities.authorizationData;

public class AuthorizationDataOutput {

	String transactionId;
	String status;
	String amount;
	String date;
	String reference;
	String batchId;
	String responseCode;
	String responseMessage;

	public AuthorizationDataOutput() {
		
	}

	public String getTransactionId() {
		return transactionId;
	}

	public AuthorizationDataOutput setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public AuthorizationDataOutput setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getAmount() {
		return amount;
	}

	public AuthorizationDataOutput setAmount(String amount) {
		this.amount = amount;
		return this;
	}

	public String getDate() {
		return date;
	}

	public AuthorizationDataOutput setDate(String date) {
		this.date = date;
		return this;
	}

	public String getReference() {
		return reference;
	}

	public AuthorizationDataOutput setReference(String reference) {
		this.reference = reference;
		return this;
	}

	public String getBatchId() {
		return batchId;
	}

	public AuthorizationDataOutput setBatchId(String batchId) {
		this.batchId = batchId;
		return this;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public AuthorizationDataOutput setResponseCode(String responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public AuthorizationDataOutput setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
		return this;
	}

}