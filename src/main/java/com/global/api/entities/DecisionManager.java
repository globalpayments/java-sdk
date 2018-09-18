package com.global.api.entities;

import com.global.api.entities.enums.Risk;

public class DecisionManager {
	private String billToHostName;
	private boolean billToHttpBrowserCookiesAccepted;
	private String billToHttpBrowserEmail;
	private String billToHttpBrowserType;
	private String billToIpNetworkAddress;
	private String businessRulessCoreThresHold;
	private String billToPersonalId;
	private String decisionManagerProfile;
	private String invoiceHeaderTenderType;
	private Risk itemHostHedge;
	private Risk itemNonsensicalHedge;
	private Risk itemObscenitiesHedge;
	private Risk itemPhoneFedge;
	private Risk itemTimeFedge;
	private Risk itemVelocityHedge;
	private boolean invoiceHeaderIsGift;
	private boolean invoiceHeaderReturnsAccepted;

	public String getBillToHostName() {
		return billToHostName;
	}
	public void setBillToHostName(String billToHostName) {
		this.billToHostName = billToHostName;
	}
	public boolean isBillToHttpBrowserCookiesAccepted() {
		return billToHttpBrowserCookiesAccepted;
	}
	public void setBillToHttpBrowserCookiesAccepted(boolean billToHttpBrowserCookiesAccepted) {
		this.billToHttpBrowserCookiesAccepted = billToHttpBrowserCookiesAccepted;
	}
	public String getBillToHttpBrowserEmail() {
		return billToHttpBrowserEmail;
	}
	public void setBillToHttpBrowserEmail(String billToHttpBrowserEmail) {
		this.billToHttpBrowserEmail = billToHttpBrowserEmail;
	}
	public String getBillToHttpBrowserType() {
		return billToHttpBrowserType;
	}
	public void setBillToHttpBrowserType(String billToHttpBrowserType) {
		this.billToHttpBrowserType = billToHttpBrowserType;
	}
	public String getBillToIpNetworkAddress() {
		return billToIpNetworkAddress;
	}
	public void setBillToIpNetworkAddress(String billToIpNetworkAddress) {
		this.billToIpNetworkAddress = billToIpNetworkAddress;
	}
	public String getBusinessRulessCoreThresHold() {
		return businessRulessCoreThresHold;
	}
	public void setBusinessRulessCoreThresHold(String businessRulessCoreThresHold) {
		this.businessRulessCoreThresHold = businessRulessCoreThresHold;
	}
	public String getBillToPersonalId() {
		return billToPersonalId;
	}
	public void setBillToPersonalId(String billToPersonalId) {
		this.billToPersonalId = billToPersonalId;
	}
	public String getDecisionManagerProfile() {
		return decisionManagerProfile;
	}
	public void setDecisionManagerProfile(String decisionManagerProfile) {
		this.decisionManagerProfile = decisionManagerProfile;
	}
	public String getInvoiceHeaderTenderType() {
		return invoiceHeaderTenderType;
	}
	public void setInvoiceHeaderTenderType(String invoiceHeaderTenderType) {
		this.invoiceHeaderTenderType = invoiceHeaderTenderType;
	}
	public Risk getItemHostHedge() {
		return itemHostHedge;
	}
	public void setItemHostHedge(Risk itemHostHedge) {
		this.itemHostHedge = itemHostHedge;
	}
	public Risk getItemNonsensicalHedge() {
		return itemNonsensicalHedge;
	}
	public void setItemNonsensicalHedge(Risk itemNonsensicalHedge) {
		this.itemNonsensicalHedge = itemNonsensicalHedge;
	}
	public Risk getItemObscenitiesHedge() {
		return itemObscenitiesHedge;
	}
	public void setItemObscenitiesHedge(Risk itemObscenitiesHedge) {
		this.itemObscenitiesHedge = itemObscenitiesHedge;
	}
	public Risk getItemPhoneFedge() {
		return itemPhoneFedge;
	}
	public void setItemPhoneFedge(Risk itemPhoneFedge) {
		this.itemPhoneFedge = itemPhoneFedge;
	}
	public Risk getItemTimeFedge() {
		return itemTimeFedge;
	}
	public void setItemTimeFedge(Risk itemTimeFedge) {
		this.itemTimeFedge = itemTimeFedge;
	}
	public Risk getItemVelocityHedge() {
		return itemVelocityHedge;
	}
	public void setItemVelocityHedge(Risk itemVelocityHedge) {
		this.itemVelocityHedge = itemVelocityHedge;
	}
	public boolean isInvoiceHeaderIsGift() {
		return invoiceHeaderIsGift;
	}
	public void setInvoiceHeaderIsGift(boolean invoiceHeaderIsGift) {
		this.invoiceHeaderIsGift = invoiceHeaderIsGift;
	}
	public boolean isInvoiceHeaderReturnsAccepted() {
		return invoiceHeaderReturnsAccepted;
	}
	public void setInvoiceHeaderReturnsAccepted(boolean invoiceHeaderReturnsAccepted) {
		this.invoiceHeaderReturnsAccepted = invoiceHeaderReturnsAccepted;
	}
}