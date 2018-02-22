package com.global.api.entities;

import java.math.BigDecimal;

public class BatchSummary {
    private Integer batchId;
    private Integer transactionCount;
    private BigDecimal totalAmount;
    private String sequenceNumber;

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
}
