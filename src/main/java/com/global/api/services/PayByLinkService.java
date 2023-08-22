package com.global.api.services;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.PayByLinkData;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.reporting.PayByLinkSummary;
import com.global.api.entities.reporting.PayByLinkSummaryPaged;

import java.math.BigDecimal;

public class PayByLinkService {

    public static AuthorizationBuilder create(PayByLinkData payByLink, BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Create)
                        .withAmount(amount)
                        .withPayByLinkData(payByLink);
    }

    public static ManagementBuilder edit(String payByLinkId) {
        return
                new ManagementBuilder(TransactionType.PayByLinkUpdate)
                        .withPaymentLinkId(payByLinkId);
    }

    public static TransactionReportBuilder<PayByLinkSummary> payByLinkDetail(String payByLinkId) {
        return
                new TransactionReportBuilder<>(ReportType.PayByLinkDetail, PayByLinkSummary.class)
                        .withPayByLinkId(payByLinkId);
    }

    public static TransactionReportBuilder<PayByLinkSummaryPaged> findPayByLink(int page, int pageSize) {
        return
                new TransactionReportBuilder<>(ReportType.FindPayByLinkPaged, PayByLinkSummaryPaged.class)
                        .withPaging(page, pageSize);
    }

}