package com.global.api.services;

import com.global.api.entities.ActivityReport;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.ReportType;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.TransactionSummaryList;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DepositSummaryList;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.DisputeSummaryList;

public class ReportingService {

    public static TransactionReportBuilder<TransactionSummaryList> findTransactions() {
        return new TransactionReportBuilder<TransactionSummaryList>(ReportType.FindTransactions, TransactionSummaryList.class);
    }

    public static TransactionReportBuilder<TransactionSummary> findTransactions(String transactionId) {
        return new TransactionReportBuilder<TransactionSummary>(ReportType.FindTransactions, TransactionSummary.class)
                .withTransactionId(transactionId);
    }

    public static TransactionReportBuilder<DisputeSummaryList> findSettlementDisputes() {
        return new TransactionReportBuilder<DisputeSummaryList>(ReportType.FindSettlementDisputes, DisputeSummaryList.class);
    }

    public static TransactionReportBuilder<DepositSummaryList> findDeposits() {
        return new TransactionReportBuilder<DepositSummaryList>(ReportType.FindDeposits, DepositSummaryList.class);
    }

    public static TransactionReportBuilder<DisputeSummaryList> findDisputes() {
        return new TransactionReportBuilder<DisputeSummaryList>(ReportType.FindDisputes, DisputeSummaryList.class);
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
                .withDepositId(depositId);
    }

    public static TransactionReportBuilder<DisputeSummary> disputeDetail(String disputeId) {
        return new TransactionReportBuilder<DisputeSummary>(ReportType.DisputeDetail, DisputeSummary.class)
                .withDisputeId(disputeId);
    }

    public static TransactionReportBuilder<DisputeSummary> settlementDisputeDetail(String settlementDisputeId) {
        return new TransactionReportBuilder<DisputeSummary>(ReportType.SettlementDisputeDetail, DisputeSummary.class)
                .WithSettlementDisputeId(settlementDisputeId);
    }

    public static TransactionReportBuilder<TransactionSummaryList> findSettlementTransactions() {
        return new TransactionReportBuilder<TransactionSummaryList>(ReportType.FindSettlementTransactions, TransactionSummaryList.class);
    }

}