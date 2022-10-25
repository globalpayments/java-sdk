package com.example.restservice.entities.authorizationData;

public class AuthorizationDataInput {

	private String cardToken;
	private String amount;
	private String currency;
	private String serverTransactionId;

	public AuthorizationDataInput() {

	}

	public String getCardToken() {
		return cardToken;
	}

	public void setCardToken(String cardToken) {
		this.cardToken = cardToken;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getServerTransactionId() {
		return serverTransactionId;
	}

	public void setServerTransactionId(String serverTransactionId) {
		this.serverTransactionId = serverTransactionId;
	}

}