package com.global.api.services;

import com.global.api.entities.ActivityReport;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.ReportType;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.TransactionSummaryList;

public class ReportingService {
    public static TransactionReportBuilder<ActivityReport> activity() {
        return new TransactionReportBuilder<ActivityReport>(ReportType.Activity, ActivityReport.class);
    }

    public static TransactionReportBuilder<TransactionSummary> transactionDetail(String transactionId) {
        return new TransactionReportBuilder<TransactionSummary>(ReportType.TransactionDetail, TransactionSummary.class)
                .withTransactionId(transactionId);
    }

    public static TransactionReportBuilder<TransactionSummaryList> findTransactions() {
        return new TransactionReportBuilder<TransactionSummaryList>(ReportType.FindTransactions, TransactionSummaryList.class);
    }

    public static TransactionReportBuilder<TransactionSummary> findTransactions(String transactionId) {
        return new TransactionReportBuilder<TransactionSummary>(ReportType.FindTransactions, TransactionSummary.class)
                .withTransactionId(transactionId);
    }
}