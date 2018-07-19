package com.global.api.entities.reporting;

import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.enums.CardType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class SearchCriteriaBuilder<TResult> {
	private TransactionReportBuilder<TResult> _reportBuilder;
	private String accountNumberLastFour;
	private String altPaymentStatus;
	private String authCode;
	private String bankRoutingNumber;
	private String batchId;
	private String batchSequenceNumber;
	private String buyerEmailAddress;
	private String cardHolderFirstName;
	private String cardHolderLastName;
	private String cardHolderPoNumber;
	private String cardNumberFirstSix;
	private String cardNumberLastFour;
	private ArrayList<CardType> cardTypes;
	private String checkFirstName;
	private String checkLastName;
	private String checkName;
	private String checkNumber;
	private String clerkId;
	private String clientTransactionId;
	private String customerId;
	private String displayName;
	private Date endDate;
	private String giftCurrency;
	private String giftMaskedAlias;
	private boolean fullyCaptured;
	private String invoiceNumber;
	private String issuerResult;
	private String issuerTransactionId;
	private boolean oneTime;
	private String paymentMethodKey;
	private ArrayList<PaymentMethodType> paymentTypes;
	private String referenceNumber;
	private ArrayList<TransactionType> transactionType;
	private BigDecimal settlementAmount;
	private String scheduleId;
	private String siteTrace;
	private Date startDate;
	private String uniqueDeviceId;
	private String username;

	public String getAccountNumberLastFour() {
		return accountNumberLastFour;
	}

	public void setAccountNumberLastFour(String accountNumberLastFour) {
		this.accountNumberLastFour = accountNumberLastFour;
	}

	public String getAltPaymentStatus() {
		return altPaymentStatus;
	}

	public void setAltPaymentStatus(String altPaymentStatus) {
		this.altPaymentStatus = altPaymentStatus;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public String getBankRoutingNumber() {
		return bankRoutingNumber;
	}

	public void setBankRoutingNumber(String bankRoutingNumber) {
		this.bankRoutingNumber = bankRoutingNumber;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getBatchSequenceNumber() {
		return batchSequenceNumber;
	}

	public void setBatchSequenceNumber(String batchSequenceNumber) {
		this.batchSequenceNumber = batchSequenceNumber;
	}

	public String getBuyerEmailAddress() {
		return buyerEmailAddress;
	}

	public void setBuyerEmailAddress(String buyerEmailAddress) {
		this.buyerEmailAddress = buyerEmailAddress;
	}

	public String getCardHolderFirstName() {
		return cardHolderFirstName;
	}

	public void setCardHolderFirstName(String cardHolderFirstName) {
		this.cardHolderFirstName = cardHolderFirstName;
	}

	public String getCardHolderLastName() {
		return cardHolderLastName;
	}

	public void setCardHolderLastName(String cardHolderLastName) {
		this.cardHolderLastName = cardHolderLastName;
	}

	public String getCardHolderPoNumber() {
		return cardHolderPoNumber;
	}

	public void setCardHolderPoNumber(String cardHolderPoNumber) {
		this.cardHolderPoNumber = cardHolderPoNumber;
	}

	public String getCardNumberFirstSix() {
		return cardNumberFirstSix;
	}

	public void setCardNumberFirstSix(String cardNumberFirstSix) {
		this.cardNumberFirstSix = cardNumberFirstSix;
	}

	public String getCardNumberLastFour() {
		return cardNumberLastFour;
	}

	public void setCardNumberLastFour(String cardNumberLastFour) {
		this.cardNumberLastFour = cardNumberLastFour;
	}

	public ArrayList<CardType> getCardTypes() {
		return cardTypes;
	}

	public void setCardTypes(ArrayList<CardType> cardTypes) {
		this.cardTypes = cardTypes;
	}

	public String getCheckFirstName() {
		return checkFirstName;
	}

	public void setCheckFirstName(String checkFirstName) {
		this.checkFirstName = checkFirstName;
	}

	public String getCheckLastName() {
		return checkLastName;
	}

	public void setCheckLastName(String checkLastName) {
		this.checkLastName = checkLastName;
	}

	public String getCheckName() {
		return checkName;
	}

	public void setCheckName(String checkName) {
		this.checkName = checkName;
	}

	public String getCheckNumber() {
		return checkNumber;
	}

	public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}

	public String getClerkId() {
		return clerkId;
	}

	public void setClerkId(String clerkId) {
		this.clerkId = clerkId;
	}

	public String getClientTransactionId() {
		return clientTransactionId;
	}

	public void setClientTransactionId(String clientTransactionId) {
		this.clientTransactionId = clientTransactionId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getGiftCurrency() {
		return giftCurrency;
	}

	public void setGiftCurrency(String giftCurrency) {
		this.giftCurrency = giftCurrency;
	}

	public String getGiftMaskedAlias() {
		return giftMaskedAlias;
	}

	public void setGiftMaskedAlias(String giftMaskedAlias) {
		this.giftMaskedAlias = giftMaskedAlias;
	}

	public boolean isFullyCaptured() {
		return fullyCaptured;
	}

	public void setFullyCaptured(boolean fullyCaptured) {
		this.fullyCaptured = fullyCaptured;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getIssuerResult() {
		return issuerResult;
	}

	public void setIssuerResult(String issuerResult) {
		this.issuerResult = issuerResult;
	}

	public String getIssuerTransactionId() {
		return issuerTransactionId;
	}

	public void setIssuerTransactionId(String issuerTransactionId) {
		this.issuerTransactionId = issuerTransactionId;
	}

	public boolean isOneTime() {
		return oneTime;
	}

	public void setOneTime(boolean oneTime) {
		this.oneTime = oneTime;
	}

	public String getPaymentMethodKey() {
		return paymentMethodKey;
	}

	public void setPaymentMethodKey(String paymentMethodKey) {
		this.paymentMethodKey = paymentMethodKey;
	}

	public ArrayList<PaymentMethodType> getPaymentTypes() {
		return paymentTypes;
	}

	public void setPaymentTypes(ArrayList<PaymentMethodType> paymentTypes) {
		this.paymentTypes = paymentTypes;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public ArrayList<TransactionType> getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(ArrayList<TransactionType> transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getSettlementAmount() {
		return settlementAmount;
	}

	public void setSettlementAmount(BigDecimal settlementAmount) {
		this.settlementAmount = settlementAmount;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getSiteTrace() {
		return siteTrace;
	}

	public void setSiteTrace(String siteTrace) {
		this.siteTrace = siteTrace;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getUniqueDeviceId() {
		return uniqueDeviceId;
	}

	public void setUniqueDeviceId(String uniqueDeviceId) {
		this.uniqueDeviceId = uniqueDeviceId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public SearchCriteriaBuilder(TransactionReportBuilder<TResult> reportBuilder) {
		_reportBuilder = reportBuilder;
	}

	public TResult execute() throws ApiException {
		return execute("default");
	}

	public TResult execute(String configName) throws ApiException {
		return _reportBuilder.execute(configName);
	}

	public <T> SearchCriteriaBuilder<TResult> and(SearchCriteria criteria, T value) {
		String criteriaValue = criteria.toString();
		if (criteriaValue != null) {
			value.toString();
		}
		return this;
	}
}