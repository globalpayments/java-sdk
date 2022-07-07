package com.global.api.services;

import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.ActivityReport;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.TransactionSummaryList;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.reporting.*;

public class ReportingService {

    public static TransactionReportBuilder<TransactionSummaryList> findTransactions() {
        return new TransactionReportBuilder<TransactionSummaryList>(ReportType.FindTransactions, TransactionSummaryList.class);
    }

    public static TransactionReportBuilder<TransactionSummary> findTransactions(String transactionId) {
        return new TransactionReportBuilder<TransactionSummary>(ReportType.FindTransactions, TransactionSummary.class)
                .withTransactionId(transactionId);
    }

    public static TransactionReportBuilder<ActivityReport> activity() {
        return new TransactionReportBuilder<ActivityReport>(ReportType.Activity, ActivityReport.class);
    }

    public static TransactionReportBuilder<TransactionSummary> transactionDetail(String transactionId) {
        return new TransactionReportBuilder<TransactionSummary>(ReportType.TransactionDetail, TransactionSummary.class)
                .withTransactionId(transactionId);
    }

    public static TransactionReportBuilder<DepositSummary> depositDetail(String depositId) {
        return new TransactionReportBuilder<DepositSummary>(ReportType.DepositDetail, DepositSummary.class)
                .withDepositReference(depositId);
    }

    public static TransactionReportBuilder<DisputeSummary> disputeDetail(String disputeId) {
        return new TransactionReportBuilder<DisputeSummary>(ReportType.DisputeDetail, DisputeSummary.class)
                .withDisputeId(disputeId);
    }

    public static TransactionReportBuilder<DisputeDocument> documentDisputeDetail(String disputeId) {
        return new TransactionReportBuilder<DisputeDocument>(ReportType.DocumentDisputeDetail, DisputeDocument.class)
                .withDisputeId(disputeId);
    }

    public static TransactionReportBuilder<DisputeSummary> settlementDisputeDetail(String settlementDisputeId) {
        return new TransactionReportBuilder<DisputeSummary>(ReportType.SettlementDisputeDetail, DisputeSummary.class)
                .withSettlementDisputeId(settlementDisputeId);
    }

    public static TransactionReportBuilder<StoredPaymentMethodSummary> storedPaymentMethodDetail(String storedPaymentMethodId) {
        return new TransactionReportBuilder<StoredPaymentMethodSummary>(ReportType.StoredPaymentMethodDetail, StoredPaymentMethodSummary.class)
                .withStoredPaymentMethodId(storedPaymentMethodId);
    }

    public static TransactionReportBuilder<ActionSummary> actionDetail(String actionId) {
        return new TransactionReportBuilder<>(ReportType.ActionDetail, ActionSummary.class)
                .withActionId(actionId);
    }

    public static TransactionReportBuilder<TransactionSummaryPaged> bankPaymentDetail(String bankPaymentId, int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindBankPayment, TransactionSummaryPaged.class)
                .withBankPaymentId(bankPaymentId)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<TransactionSummaryPaged> findBankPaymentTransactions(int page, int pageSize)
    {
        return new TransactionReportBuilder<>(ReportType.FindBankPayment, TransactionSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<TransactionSummaryPaged> findTransactionsPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindTransactionsPaged, TransactionSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<TransactionSummaryPaged> findTransactionsPaged(int page, int pageSize, String transactionId) {
        return new TransactionReportBuilder<>(ReportType.FindTransactionsPaged, TransactionSummaryPaged.class)
                .withTransactionId(transactionId)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<TransactionSummaryPaged> findSettlementTransactionsPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindSettlementTransactionsPaged, TransactionSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<DepositSummaryPaged> findDepositsPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindDepositsPaged, DepositSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<DisputeSummaryPaged> findDisputesPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindDisputesPaged, DisputeSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<DisputeSummaryPaged> findSettlementDisputesPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindSettlementDisputesPaged, DisputeSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<StoredPaymentMethodSummaryPaged> findStoredPaymentMethodsPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindStoredPaymentMethodsPaged, StoredPaymentMethodSummaryPaged.class)
                .withPaging(page, pageSize);
    }

    public static TransactionReportBuilder<ActionSummaryPaged> findActionsPaged(int page, int pageSize) {
        return new TransactionReportBuilder<>(ReportType.FindActionsPaged, ActionSummaryPaged.class)
                .withPaging(page, pageSize);
    }

}