package com.global.api.entities;

import com.global.api.builders.ResubmitBuilder;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class BatchSummary {
    private Integer batchId;
    private Integer transactionCount;
    private BigDecimal totalAmount;
    private String sequenceNumber;
    private String responseCode;
    private LinkedList<Transaction> resentTransactions;
    private Transaction resentBatchClose;
    private String transactionToken;

    public Integer getBatchId() {
        return batchId;
    }
    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }
    public Integer getTransactionCount() {
        return transactionCount;
    }
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    public boolean isBalanced() {
        if(responseCode != null) {
            return responseCode.equals("500");
        }
        return false;
    }
    public LinkedList<Transaction> getResentTransactions() {
        return resentTransactions;
    }
    public void setResentTransactions(LinkedList<Transaction> resentTransactions) {
        this.resentTransactions = resentTransactions;
    }
    public Transaction getResentBatchClose() {
        return resentBatchClose;
    }
    public void setResentBatchClose(Transaction resentBatchClose) {
        this.resentBatchClose = resentBatchClose;
    }
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public String getTransactionToken() {
        return transactionToken;
    }
    public void setTransactionToken(String transactionToken) {
        this.transactionToken = transactionToken;
    }

    public BatchSummary resubmitTransactions(List<String> transactionTokens) throws ApiException {
        return resubmitTransactions(transactionTokens, "default");
    }
    public BatchSummary resubmitTransactions(List<String> transactionTokens, String configName) throws ApiException {
        if(!this.responseCode.equals("580")) {
            throw new BuilderException("Batch recovery has not been requested for this batch.");
        }

        // resubmit the tokens
        LinkedList<Transaction> responses = new LinkedList<Transaction>();
        for(String token: transactionTokens) {
            Transaction response = new ResubmitBuilder(TransactionType.DataCollect)
                    .withTransactionToken(token)
                    .execute(configName);
            responses.add(response);
        }
        this.setResentTransactions(responses);

        // resubmit the batch summary
        Transaction batchResponse = new ResubmitBuilder(TransactionType.BatchClose)
                .withTransactionToken(this.transactionToken)
                .execute(configName);
        this.setResentBatchClose(batchResponse);
        this.setResponseCode(batchResponse.getResponseCode());
        return this;
    }
}
