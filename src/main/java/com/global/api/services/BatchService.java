package com.global.api.services;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

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

//    public static BatchSummary closeBatch(BatchCloseType closeType) throws ApiException {
//        return closeBatch(closeType, "default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(int batchNumber) throws ApiException {
//        return closeBatch(batchNumber, "default");
//    }
//    public static BatchSummary closeBatch(int batchNumber, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(int batchNumber, int sequenceNumber) throws ApiException {
//        return closeBatch(batchNumber, sequenceNumber, "default");
//    }
//    public static BatchSummary closeBatch(int batchNumber, int sequenceNumber, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber, sequenceNumber)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber) throws ApiException {
//        return closeBatch(closeType, batchNumber, "default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber) throws ApiException {
//        return closeBatch(closeType, batchNumber, sequenceNumber,"default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber, sequenceNumber)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(BatchCloseType closeType, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(closeType, transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(batchNumber, transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(batchNumber, sequenceNumber, transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber, sequenceNumber)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(closeType, batchNumber, transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
//
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits) throws ApiException {
//        return closeBatch(closeType, batchNumber, sequenceNumber, transactionTotal, totalCredits, totalDebits, "default");
//    }
//    public static BatchSummary closeBatch(BatchCloseType closeType, int batchNumber, int sequenceNumber, int transactionTotal, BigDecimal totalCredits, BigDecimal totalDebits, String configName) throws ApiException {
//        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
//                .withBatchNumber(batchNumber, sequenceNumber)
//                .withBatchTotals(transactionTotal, totalDebits, totalCredits)
//                .withBatchCloseType(closeType)
//                .execute(configName);
//        return response.getBatchSummary();
//    }
}
