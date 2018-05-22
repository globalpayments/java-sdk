package com.global.api.entities;

import java.math.BigDecimal;

public class DccResponseResult {

	private String cardHolderCurrency = null;
	private BigDecimal cardHolderAmount = null;
	private BigDecimal cardHolderRate = null;
	private String merchantCurrency = null;
	private BigDecimal merchantAmount = null;
	public String getCardHolderCurrency() {
		return cardHolderCurrency;
	}
	public void setCardHolderCurrency(String cardHolderCurrency) {
		this.cardHolderCurrency = cardHolderCurrency;
	}
	public BigDecimal getCardHolderAmount() {
		return cardHolderAmount;
	}
	public void setCardHolderAmount(BigDecimal cardHolderAmount) {
		this.cardHolderAmount = cardHolderAmount;
	}
	public BigDecimal getCardHolderRate() {
		return cardHolderRate;
	}
	public void setCardHolderRate(BigDecimal cardHolderRate) {
		this.cardHolderRate = cardHolderRate;
	}
	public String getMerchantCurrency() {
		return merchantCurrency;
	}
	public void setMerchantCurrency(String merchantCurrency) {
		this.merchantCurrency = merchantCurrency;
	}
	public BigDecimal getMerchantAmount() {
		return merchantAmount;
	}
	public void setMerchantAmount(BigDecimal merchantAmount) {
		this.merchantAmount = merchantAmount;
	}

}
