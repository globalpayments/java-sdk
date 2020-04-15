package com.global.api.terminals.ingenico.responses;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.ITerminalResponse;
import com.global.api.terminals.ingenico.variables.TransactionStatus;
import com.global.api.terminals.ingenico.variables.TransactionSubTypes;
import com.global.api.utils.Extensions;
import com.global.api.terminals.ingenico.variables.DynamicCurrencyStatus;
import com.global.api.terminals.ingenico.variables.PaymentMode;

public class IngenicoTerminalResponse extends IngenicoBaseResponse implements ITerminalResponse, IDeviceResponse {

	private TransactionStatus transactionStatus;
	private BigDecimal _amount;
	private PaymentMode _paymentMode;
	private String _privateData;
	private String _currencyCode;
	private DataResponse _respField;

	public IngenicoTerminalResponse(byte[] buffer) {
		parseResponse(buffer);
	}

	public String dccCurrency;
	public DynamicCurrencyStatus dccStatus;
	public TransactionSubTypes transactionSubType;
	public BigDecimal splitSaleAmount;
	public String dynamicCurrencyCode;
	public String currencyCode;
	public String privateData;
	public BigDecimal finalTransactionAmount;

	public String getDccCurrency() {
		return _respField.getDccCode();
	}

	public void setDccCurrency(String dccCurrency) {
		this.dccCurrency = dccCurrency;
	}

	public DynamicCurrencyStatus getDccStatus() {
		return _respField.getDccStatus();
	}

	public void setDccStatus(DynamicCurrencyStatus dccStatus) {
		this.dccStatus = dccStatus;
	}

	public TransactionSubTypes getTransactionSubType() {
		return _respField.getTransactionSubType();
	}

	public void setSplitSaleAmount(TransactionSubTypes transactionSubType) {
		this.transactionSubType = transactionSubType;
	}

	public BigDecimal getSplitSaleAmount() {
		return new BigDecimal("0");
	}

	public void setSplitSaleAmount(BigDecimal splitSaleAmount) {
		this.splitSaleAmount = splitSaleAmount;
	}

	public PaymentMode getPaymentMode() {
		return _paymentMode;
	}

	public void setPaymentMode(PaymentMode paymentMode) {
		_paymentMode = paymentMode;
	}

	public String getDynamicCurrencyCode() {
		return _respField.getDccCode();
	}

	public void setDynamicCurrencyCode(String dynamicCurrencyCode) {
		this.dynamicCurrencyCode = dynamicCurrencyCode;
	}

	public String getCurrencyCode() {
		return _currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		_currencyCode = currencyCode;
	}

	public String getPrivateData() {
		return _privateData;
	}

	public void setPrivateData(String privateData) {
		_privateData = privateData;
	}

	public BigDecimal getFinalTransactionAmount() {
		return _respField.getFinalAmount();
	}

	public void setFinalTransactionAmount(BigDecimal finalTransactionAmount) {
		this.finalTransactionAmount = finalTransactionAmount;
	}

	// properties
	public String ResponseText;
	public BigDecimal TransactionAmount;
	public BigDecimal BalanceAmount;
	public String AuthorizationCode;
	public BigDecimal TipAmount;
	public BigDecimal CashBackAmount;
	public String PaymentType;
	public String TerminalRefNumber;
	public String ResponseCode;
	public String TransactionId;
	public String Token;
	public String SignatureStatus;
	public byte[] SignatureData;
	public String TransactionType;
	public String MaskedCardNumber;
	public String EntryMethod;
	public String ApprovalCode;
	public BigDecimal AmountDue;
	public String CardHolderName;
	public String CardBIN;
	public boolean CardPresent;
	public String ExpirationDate;
	public String AvsResponseCode;
	public String AvsResponseText;
	public String CvvResponseCode;
	public String CvvResponseText;
	public boolean TaxExempt;
	public String TaxExemptId;
	public String TicketNumber;
	public ApplicationCryptogramType ApplicationCryptogramType;
	public String ApplicationCryptogram;
	public String CardHolderVerificationMethod;
	public String TerminalVerificationResults;
	public String ApplicationPreferredName;
	public String ApplicationLabel;
	public String ApplicationId;

	public String getResponseText() {
		return ResponseText;
	}

	public void setResponseText(String responseText) {
		ResponseText = responseText;
	}

	public BigDecimal getTransactionAmount() {
		return Extensions.toAmount(_amount.toString());
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		TransactionAmount = transactionAmount;
	}

	public BigDecimal getBalanceAmount() {
		return _respField.getAvailableAmount();
	}

	public void setBalanceAmount(BigDecimal balanceAmount) {
		BalanceAmount = balanceAmount;
	}

	public String getAuthorizationCode() {
		return _respField.getAuthorizationCode();
	}

	public void setAuthorizationCode(String authorizationCode) {
		AuthorizationCode = authorizationCode;
	}

	public BigDecimal getTipAmount() {
		return _respField.getGratuityAmount();
	}

	public void setTipAmount(BigDecimal tipAmount) {
		TipAmount = tipAmount;
	}

	public BigDecimal getCashBackAmount() {
		return _respField.getCashbackAmount();
	}

	public void setCashBackAmount(BigDecimal cashBackAmount) {
		CashBackAmount = cashBackAmount;
	}

	public String getPaymentType() {
		return (_respField.getPaymentMethod() == null ? "" : _respField.getPaymentMethod().toString());
	}

	public void setPaymentType(String paymentType) {
		PaymentType = paymentType;
	}

	public String getTerminalRefNumber() {
		return TerminalRefNumber;
	}

	public void setTerminalRefNumber(String terminalRefNumber) {
		referenceNumber = terminalRefNumber;
	}

	public String getResponseCode() {
		return ResponseCode;
	}

	public void setResponseCode(String responseCode) {
		ResponseCode = responseCode;
	}

	public String getTransactionId() {
		return TransactionId;
	}

	public void setTransactionId(String transactionId) {
		TransactionId = transactionId;
	}

	public String getToken() {
		return Token;
	}

	public void setToken(String token) {
		Token = token;
	}

	public String getSignatureStatus() {
		return SignatureStatus;
	}

	public void setSignatureStatus(String signatureStatus) {
		SignatureStatus = signatureStatus;
	}

	public byte[] getSignatureData() {
		return SignatureData;
	}

	public void setSignatureData(byte[] signatureData) {
		SignatureData = signatureData;
	}

	public String getTransactionType() {
		return TransactionType;
	}

	public void setTransactionType(String transactionType) {
		TransactionType = transactionType;
	}

	public String getMaskedCardNumber() {
		return MaskedCardNumber;
	}

	public void setMaskedCardNumber(String maskedCardNumber) {
		MaskedCardNumber = maskedCardNumber;
	}

	public String getEntryMethod() {
		return EntryMethod;
	}

	public void setEntryMethod(String entryMethod) {
		EntryMethod = entryMethod;
	}

	public String getApprovalCode() {
		return ApprovalCode;
	}

	public void setApprovalCode(String approvalCode) {
		ApprovalCode = approvalCode;
	}

	public BigDecimal getAmountDue() {
		return AmountDue;
	}
	
	public void setAmountDue(BigDecimal amountDue) {
		AmountDue = amountDue;
	}

	public String getCardHolderName() {
		return CardHolderName;
	}

	public  void setCardHolderName(String cardHolderName) {
		CardHolderName = cardHolderName;
	}

	public String getCardBIN() {
		return CardBIN;
	}

	public void setCardBIN(String cardBIN) {
		CardBIN = cardBIN;
	}

	public boolean getCardPresent() {
		return CardPresent;
	}

	public void setCardPresent(boolean cardPresent) {
		CardPresent = cardPresent;
	}

	public String getExpirationDate() {
		return ExpirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		ExpirationDate = expirationDate;
	}

	public String getAvsResponseCode() {
		return AvsResponseCode;
	}

	public void setAvsResponseCode(String avsResponseCode) {
		AvsResponseCode = avsResponseCode;
	}

	public String getAvsResponseText() {
		return AvsResponseText;
	}

	public void setAvsResponseText(String avsResponseText) {
		AvsResponseText = avsResponseText;
	}

	public String getCvvResponseCode() {
		return CvvResponseCode;
	}

	public void setCvvResponseCode(String cvvResponseCode) {
		CvvResponseCode = cvvResponseCode;
	}

	public String getCvvResponseText() {
		return CvvResponseText;
	}

	public void setCvvResponseText(String cvvResponseText) {
		CvvResponseText = cvvResponseText;
	}

	public boolean getTaxExempt() {
		return TaxExempt;
	}

	public void setTaxExempt(boolean taxExempt) {
		TaxExempt = taxExempt;
	}

	public String getTaxExemptId() {
		return TaxExemptId;
	}

	public void setTaxExemptId(String taxExemptId) {
		TaxExemptId = taxExemptId;
	}

	public String getTicketNumber() {
		return TicketNumber;
	}

	public void setTicketNumber(String ticketNumber) {
		TicketNumber = ticketNumber;
	}

	public ApplicationCryptogramType getApplicationCryptogramType() {
		return ApplicationCryptogramType;
	}

	public void setApplicationCryptogramType(ApplicationCryptogramType applicationCryptogramType) {
		ApplicationCryptogramType = applicationCryptogramType;
	}

	public String getApplicationCryptogram() {
		return ApplicationCryptogram;
	}

	public void setApplicationCryptogram(String applicationCryptogram) {
		ApplicationCryptogram = applicationCryptogram;
	}

	public String getCardHolderVerificationMethod() {
		return CardHolderVerificationMethod;
	}

	public void setCardHolderVerificationMethod(String cardHolderVerificationMethod) {
		CardHolderVerificationMethod = cardHolderVerificationMethod;
	}

	public String getTerminalVerificationResults() {
		return TerminalVerificationResults;
	}

	public void setTerminalVerificationResults(String terminalVerificationResults) {
		TerminalVerificationResults = terminalVerificationResults;
	}

	public String getApplicationPreferredName() {
		return ApplicationPreferredName;
	}

	public void setApplicationPreferredName(String applicationPreferredName) {
		ApplicationPreferredName = applicationPreferredName;
	}

	public String getApplicationLabel() {
		return ApplicationLabel;
	}

	public void setApplicationLabel(String applicationLabel) {
		ApplicationLabel = applicationLabel;
	}

	public String getApplicationId() {
		return ApplicationId;
	}

	public void setApplicationId(String applicationId) {
		ApplicationId = applicationId;
	}

	public void parseResponse(byte[] response) {
		if (response != null) {
			rawData = new String(response, StandardCharsets.UTF_8);
			referenceNumber = rawData.substring(0, 2);
			transactionStatus = TransactionStatus.getEnumName(Integer.parseInt(rawData.substring(2, 3)));
			_amount = new BigDecimal(rawData.substring(3, 11));
			_paymentMode = PaymentMode.getEnumName(Integer.parseInt(rawData.substring(11, 12)));
			_respField = new DataResponse(rawData.substring(12, 67).getBytes());
			_currencyCode = rawData.substring(67, 70);
			_privateData = rawData.substring(70, rawData.length());
			status = transactionStatus.toString();
		}
	}
}