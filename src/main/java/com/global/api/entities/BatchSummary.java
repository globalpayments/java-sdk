package com.global.api.entities;

import com.global.api.builders.ResubmitBuilder;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class BatchSummary {
    private Integer batchId;
    private String closeTransactionId;
    private Integer closeCount;
    private BigDecimal creditAmount;
    private Integer creditCount;
    private BigDecimal debitAmount;
    private Integer debitCount;
    private String deviceId;
    private String merchantName;
    private DateTime openTime;
    private String openTransactionId;
    private Transaction resentBatchClose;
    private LinkedList<Transaction> resentTransactions;
    private String responseCode;
    private BigDecimal returnAmount;
    private Integer returnCount;
    private BigDecimal saleAmount;
    private Integer saleCount;
    private String sequenceNumber;
    private String siteId;
    private String status;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private String transactionToken;

    public boolean isBalanced() {
        if(responseCode != null) {
            return responseCode.equals("500");
        }
        return false;
    }

    public Integer getBatchId() {
        return batchId;
    }
    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }
    public String getCloseTransactionId() {
        return closeTransactionId;
    }
    public void setCloseTransactionId(String closeTransactionId) {
        this.closeTransactionId = closeTransactionId;
    }
    public Integer getCloseCount() {
        return closeCount;
    }
    public void setCloseCount(Integer closeCount) {
        this.closeCount = closeCount;
    }
    public BigDecimal getCreditAmount() {
        return creditAmount;
    }
    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }
    public Integer getCreditCount() {
        return creditCount;
    }
    public void setCreditCount(Integer creditCount) {
        this.creditCount = creditCount;
    }
    public BigDecimal getDebitAmount() {
        return debitAmount;
    }
    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }
    public Integer getDebitCount() {
        return debitCount;
    }
    public void setDebitCount(Integer debitCount) {
        this.debitCount = debitCount;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getMerchantName() {
        return merchantName;
    }
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public DateTime getOpenTime() {
        return openTime;
    }
    public void setOpenTime(DateTime openTime) {
        this.openTime = openTime;
    }
    public String getOpenTransactionId() {
        return openTransactionId;
    }
    public void setOpenTransactionId(String openTransactionId) {
        this.openTransactionId = openTransactionId;
    }
    public Transaction getResentBatchClose() {
        return resentBatchClose;
    }
    public void setResentBatchClose(Transaction resentBatchClose) {
        this.resentBatchClose = resentBatchClose;
    }
    public LinkedList<Transaction> getResentTransactions() {
        return resentTransactions;
    }
    public void setResentTransactions(LinkedList<Transaction> resentTransactions) {
        this.resentTransactions = resentTransactions;
    }
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public BigDecimal getReturnAmount() {
        return returnAmount;
    }
    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }
    public Integer getReturnCount() {
        return returnCount;
    }
    public void setReturnCount(Integer returnCount) {
        this.returnCount = returnCount;
    }
    public BigDecimal getSaleAmount() {
        return saleAmount;
    }
    public void setSaleAmount(BigDecimal saleAmount) {
        this.saleAmount = saleAmount;
    }
    public Integer getSaleCount() {
        return saleCount;
    }
    public void setSaleCount(Integer saleCount) {
        this.saleCount = saleCount;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public Integer getTransactionCount() {
        return transactionCount;
    }
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
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
