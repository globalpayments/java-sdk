package com.global.api.services;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.PayLinkData;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.reporting.PayLinkSummary;
import com.global.api.entities.reporting.PayLinkSummaryPaged;

import java.math.BigDecimal;

public class PayLinkService {

    public static AuthorizationBuilder create(PayLinkData payLink, BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Create)
                        .withAmount(amount)
                        .withPayLinkData(payLink);
    }

    public static ManagementBuilder edit(String payLinkId) {
        return
                new ManagementBuilder(TransactionType.PayLinkUpdate)
                        .withPaymentLinkId(payLinkId);
    }

    public static TransactionReportBuilder<PayLinkSummary> payLinkDetail(String payLinkId) {
        return
                new TransactionReportBuilder<>(ReportType.PayLinkDetail, PayLinkSummary.class)
                        .withPayLinkId(payLinkId);
    }

    public static TransactionReportBuilder<PayLinkSummaryPaged> findPayLink(int page, int pageSize) {
        return
                new TransactionReportBuilder<PayLinkSummaryPaged>(ReportType.FindPayLinkPaged, PayLinkSummaryPaged.class)
                        .withPaging(page, pageSize);
    }

}