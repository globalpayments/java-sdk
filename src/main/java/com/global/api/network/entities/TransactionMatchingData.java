package com.global.api.network.entities;

public class TransactionMatchingData {
    private String originalBatchNumber;
    private String originalDate;

    public String getOriginalBatchNumber() {
        return originalBatchNumber;
    }
    public void setOriginalBatchNumber(String originalBatchNumber) {
        this.originalBatchNumber = originalBatchNumber;
    }
    public String getOriginalDate() {
        return originalDate;
    }
    public void setOriginalDate(String originalDate) {
        this.originalDate = originalDate;
    }
    public String getElementData() {
        if(originalBatchNumber != null && originalDate != null) {
            return originalBatchNumber.concat(originalDate);
        }
        return null;
    }

    public TransactionMatchingData(String batchNumber, String date) {
        originalBatchNumber = batchNumber;
        originalDate = date;
    }
}
