package com.global.api.services;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE123_ReconciliationTotals_nws;
import com.global.api.network.entities.gnap.GnapRequestData;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import java.math.BigDecimal;

public class BatchService {
    public static BatchSummary closeBatch() throws ApiException {
        return closeBatch("default");
    }
    public static BatchSummary closeBatch(String configName) throws ApiException {
        Transaction response = new ManagementBuilder(TransactionType.BatchClose).execute(configName);
        return response.getBatchSummary();
    }

    public static ManagementBuilder closeBatch(BatchCloseType closeType) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchCloseType(closeType);
    }
    public static ManagementBuilder closeBatch(int batchNumber) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber);
    }
    public static ManagementBuilder closeBatch(int batchNumber, int sequenceNumber) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber, sequenceNumber);
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, int batchNumber) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber)
                .withBatchCloseType(closeType);
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber, sequenceNumber)
                .withBatchCloseType(closeType);
    }
    public static ManagementBuilder closeBatch(int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits);
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
                .withBatchCloseType(closeType);
    }
    public static ManagementBuilder closeBatch(int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits);
    }
    public static ManagementBuilder closeBatch(int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber, sequenceNumber)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits);
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
                .withBatchCloseType(closeType);
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber, sequenceNumber)
                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
                .withBatchCloseType(closeType);
    }

    public static ManagementBuilder closeBatch(BatchCloseType closeType, NtsRequestMessageHeader ntsRequestMessageHeader, int batchNumber,
                                               int transactionCount, BigDecimal totalSales, BigDecimal totalReturns, NtsRequestToBalanceData requestToBalanceData) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber)
                .withBatchTotalTransaction(transactionCount, totalSales, totalReturns)
                .withBatchCloseType(closeType)
                .withNtsRequestsToBalanceData(requestToBalanceData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader);

    }

    public static BatchSummary closeBatch(String batchReference, String configName) throws ApiException {
        Transaction response =
                new ManagementBuilder(TransactionType.BatchClose)
                        .withBatchReference(batchReference)
                        .execute(configName);

        return response.getBatchSummary();
    }
    public static ManagementBuilder closeBatch(BatchCloseType closeType, GnapRequestData data) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withCurrency("USD")
                .withGnapRequestData(data)
                .withBatchCloseType(closeType);
    }

    public static ManagementBuilder closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber, int transactionTotal, DE123_ReconciliationTotals_nws totals) {
        return new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchNumber, sequenceNumber)
                .withBatchTotals(transactionTotal,totals)
                .withBatchCloseType(closeType);
    }
}
