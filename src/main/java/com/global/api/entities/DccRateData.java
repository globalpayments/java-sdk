package com.global.api.entities;

import java.math.BigDecimal;

public class DccRateData {	
	
	private BigDecimal amount = null;
	private String currency = null;
	private String dccProcessor = null;
	private BigDecimal dccRate = null;
	private String dccRateType = null;
	private String dccType = null;
	private String oredrId = null;

	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getDccProcessor() {
		return dccProcessor;
	}
	public void setDccProcessor(String dccProcessor) {
		this.dccProcessor = dccProcessor;
	}
	public BigDecimal getDccRate() {
		return dccRate;
	}
	public void setDccRate(BigDecimal dccRate) {
		this.dccRate = dccRate;
	}
	public String getDccRateType() {
		return dccRateType;
	}
	public void setDccRateType(String dccRateType) {
		this.dccRateType = dccRateType;
	}
	public String getDccType() {
		return dccType;
	}
	public void setDccType(String dccType) {
		this.dccType = dccType;
	}
	public String getOredrId() {
		return oredrId;
	}
	public void setOredrId(String oredrId) {
		this.oredrId = oredrId;
	}
	
}
