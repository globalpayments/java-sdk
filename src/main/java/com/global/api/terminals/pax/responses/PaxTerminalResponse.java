package com.global.api.terminals.pax.responses;

import java.math.BigDecimal;

import com.global.api.entities.enums.PaxMsgId;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.terminals.abstractions.ITerminalResponse;
import com.global.api.terminals.ingenico.variables.DynamicCurrencyStatus;
import com.global.api.terminals.ingenico.variables.PaymentMethod;
import com.global.api.terminals.ingenico.variables.PaymentMode;
import com.global.api.terminals.ingenico.variables.TransactionSubTypes;

public class PaxTerminalResponse extends PaxBaseResponse implements ITerminalResponse {

	public PaxTerminalResponse(byte[] buffer, PaxMsgId[] messageIds) throws MessageException {
		super(buffer, messageIds);
		// TODO Auto-generated constructor stub
	}

	public boolean getCardPresent() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getTaxExempt() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCardHolderVerificationMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCardHolderVerificationMethod(String cardHolderVerificationMethod) {
		// TODO Auto-generated method stub
		
	}

	public String getCurrencyCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCurrencyCode(String currencyCode) {
		// TODO Auto-generated method stub
		
	}

	public String getPrivateData() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPrivateData(String privateData) {
		// TODO Auto-generated method stub
		
	}

	public BigDecimal getFinalTransactionAmount() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFinalTransactionAmount(BigDecimal finalTransactionAmount) {
		// TODO Auto-generated method stub
		
	}

	public String getPaymentMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		// TODO Auto-generated method stub
		
	}

	public String getTransactionSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTransactionSubType(TransactionSubTypes transactionSubType) {
		// TODO Auto-generated method stub
		
	}

	public BigDecimal getSplitSaleAmount() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSplitSaleAmount(BigDecimal splitSaleAmount) {
		// TODO Auto-generated method stub
		
	}

	public BigDecimal getDynamicCurrencyCodeAmount() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDyanmicCurrencyCodeAmount(BigDecimal dynamicCurrencyCodeAmount) {
		// TODO Auto-generated method stub
		
	}

	public String getDynamicCurrencyCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDynamicCurrencyCode(String dynamicCurrencyCode) {
		// TODO Auto-generated method stub
		
	}

	public String getDynamicCurrencyCodeStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDynamicCurrencyCodeStatus(DynamicCurrencyStatus status) {
		// TODO Auto-generated method stub
		
	}

	public String getPaymentMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPaymentMode(PaymentMode paymentMode) {
		// TODO Auto-generated method stub
		
	}

}
