package com.global.api.services;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE123_ReconciliationTotals_nws;
import com.global.api.network.entities.gnap.GnapRequestData;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import java.math.BigDecimal;

public class BatchService {
    public static final String DEFAULT_CONFIG_NAME  = "default";
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

    /**
     * Closes the current batch for the given currency and payment method filters
     * using the default service configuration.
     *
     * @param currency the transaction currency to close the batch for
     * @param paymentMethods the payment method names to include in the close request
     * @return the resulting batch summary
     * @throws ApiException if the batch close request fails
     */
    public static BatchSummary closeBatch(String currency, PaymentMethodName[] paymentMethods) throws ApiException {
        return closeBatch(currency, paymentMethods,DEFAULT_CONFIG_NAME );
    }

    /**
     * Closes the current batch for the given currency and payment method filters
     * using the specified service configuration.
     *
     * @param currency the transaction currency to close the batch for
     * @param paymentMethods the payment method names to include in the close request
     * @param configName the configured service name to execute the request against
     * @return the resulting batch summary
     * @throws ApiException if the batch close request fails
     */
    public static BatchSummary closeBatch(String currency, PaymentMethodName[] paymentMethods, String configName) throws ApiException {
        Transaction response = new ManagementBuilder(TransactionType.BatchClose)
                .withCurrency(currency)
                .withPaymentMethodNames(paymentMethods)
                .execute(configName);
        return response.getBatchSummary();
    }
}
