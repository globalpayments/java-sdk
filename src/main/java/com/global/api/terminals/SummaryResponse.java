package com.global.api.terminals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.SummaryType;

public class SummaryResponse {
	public BigDecimal amount;
	public BigDecimal amountDue;
	public BigDecimal authorizedAmount;
	public int count;
	public SummaryType summaryType;
	public BigDecimal totalAmount;
	public List<TransactionSummary> transactions;
	
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getAmountDue() {
		return amountDue;
	}
	public void setAmountDue(BigDecimal amountDue) {
		this.amountDue = amountDue;
	}
	public BigDecimal getAuthorizedAmount() {
		return authorizedAmount;
	}
	public void setAuthorizedAmount(BigDecimal authorizedAmount) {
		this.authorizedAmount = authorizedAmount;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public SummaryType getSummaryType() {
		return summaryType;
	}
	public void setSummaryType(SummaryType summaryType) {
		this.summaryType = summaryType;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	public List<TransactionSummary> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<TransactionSummary> transactions) {
		this.transactions = transactions;
	}

	public SummaryResponse() {
	    transactions = new ArrayList<TransactionSummary>();
    }
}