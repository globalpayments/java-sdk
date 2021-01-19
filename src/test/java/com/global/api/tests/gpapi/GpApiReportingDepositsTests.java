package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DepositSummaryList;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.enums.DepositSortProperty;
import com.global.api.entities.enums.DepositStatus;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.DATE_SDF;
import static org.junit.Assert.*;

public class GpApiReportingDepositsTests {

    private static Date DATE_2020_01_01 = null;

    static {
        try {
            DATE_2020_01_01 = DATE_SDF.parse("2020-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public GpApiReportingDepositsTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        // Pablo Credentials
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh");

        // Nacho credentials
        //        config
        //                .setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll")
        //                .setAppKey("QDsW1ETQKHX6Y4TA");

        ServicesContainer.configureService(config, "GpApiConfig");
    }

    // ================================================================================
    // Deposits
    // ================================================================================

    @Test
    public void reportDepositDetail() throws ApiException {
        String depositId = "DEP_2342423423";

        DepositSummary deposit =
                ReportingService
                        .depositDetail(depositId)
                        .execute("GpApiConfig");

        assertNotNull(deposit);
        assertEquals(depositId, deposit.getDepositId());
    }

    @Test
    public void reportDepositDetail_WrongId() throws ApiException {
        String depositId = "DEP_234242342";
        try {
            ReportingService
                    .depositDetail(depositId)
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Deposits DEP_234242342 not found at this /ucp/settlement/deposits/DEP_234242342", ex.getMessage());
        }
    }

    @Test
    public void reportFindDepositsWithCriteria() throws ApiException {
        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .orderBy(DepositSortProperty.TimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
//                        .and(SearchCriteria.EndDate, new Date())
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), DATE_2020_01_01));
        }
    }

    @Test
    public void reportFindDeposits_FilterBy_StartDate_OrderBy_TimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);

        List<DepositSummary> deposits = ReportingService.findDeposits()
                .orderBy(DepositSortProperty.TimeCreated, SortDirection.Descending)
                .withPaging(1, 10)
                .where(SearchCriteria.StartDate, startDate)
                .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), startDate));
        }
    }

    @Test
    public void reportFindDeposits_OrderBy_DepositId() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);

        List<DepositSummary> deposits = ReportingService.findDeposits()
                .orderBy(DepositSortProperty.DepositId, SortDirection.Descending)
                .withPaging(1, 10)
                .where(SearchCriteria.StartDate, startDate)
                .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), startDate));
        }
    }

    @Test
    public void reportFindDeposits_OrderBy_Status() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);

        List<DepositSummary> deposits = ReportingService.findDeposits()
                .orderBy(DepositSortProperty.Status, SortDirection.Ascending)
                .withPaging(1, 10)
                .where(SearchCriteria.StartDate, startDate)
                .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), startDate));
        }
    }

    @Test
    public void reportFindDeposits_OrderBy_Type() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);

        List<DepositSummary> deposits = ReportingService.findDeposits()
                .orderBy(DepositSortProperty.Type, SortDirection.Descending)
                .withPaging(1, 10)
                .where(SearchCriteria.StartDate, startDate)
                .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), startDate));
        }

        DepositSummaryList depositsAsc =
                ReportingService
                        .findDeposits()
                        .orderBy(DepositSortProperty.Type, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(depositsAsc);

        assertNotSame(deposits, depositsAsc);
    }

    @Test
    public void compareResults_reportFindDeposits_OrderBy_DepositId_And_Type() throws ApiException {
        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .orderBy(DepositSortProperty.DepositId, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(deposits);

        DepositSummaryList depositsType =
                ReportingService
                        .findDeposits()
                        .orderBy(DepositSortProperty.Type, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(depositsType);

        assertNotSame(deposits, depositsType);
    }

    @Test
    public void reportFindDeposits_FilterBy_DepositId() throws ApiException {
        String depositId = "DEP_2342423423";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .withDepositId(depositId)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        assertEquals(1, deposits.size());
        assertEquals(depositId, deposits.get(0).getDepositId());
    }

    @Test
    public void reportFindDeposits_FilterBy_WrongDepositId() throws ApiException {
        String depositId = UUID.randomUUID().toString().replace("-", "");

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .withDepositId(depositId)
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        assertEquals(0, deposits.size());
    }

    @Test
    public void reportFindDeposits_FilterBy_Status() throws ApiException {
        //"FUNDED", "SPLIT_FUNDING", "DELAYED", "RESERVED", "IRREG", "RELEASED"
        for (DepositStatus depositStatus : DepositStatus.values()) {
            DepositSummaryList deposits =
                    ReportingService
                            .findDeposits()
                            .where(SearchCriteria.StartDate, DATE_2020_01_01)
                            .and(SearchCriteria.DepositStatus, depositStatus)
                            .execute("GpApiConfig");

            assertNotNull(deposits);
            for (DepositSummary depositSummary : deposits) {
                assertEquals(depositStatus.toString().toUpperCase().trim().replace("IRREGULAR", "IRREG"),
                        depositSummary.getStatus().trim().replace(" ", ""));
            }
        }
    }

    //TODO - empty list returned
    @Test
    public void reportFindDeposits_FilterBy_StartAndEndDate() throws ApiException {
        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(SearchCriteria.EndDate, DateUtils.addDays(new Date(), -2))
                        .execute("GpApiConfig");

        assertNotNull(deposits);
    }

    @Test
    public void reportFindDeposits_FilterBy_Amount() throws ApiException {
        BigDecimal amount = new BigDecimal("114");

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertEquals(amount.multiply(new BigDecimal("100")), depositSummary.getAmount());
        }
    }

    @Test
    public void reportFindDeposits_FilterBy_NotFoundAmount() throws ApiException {
        BigDecimal amount = new BigDecimal("1");

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        assertEquals(0, deposits.size());
    }

    @Test
    public void reportFindDeposits_FilterBy_MaskedAccountNumberLast4() throws ApiException {
        String masked_account_number_last4 = "9999";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(SearchCriteria.AccountNumberLastFour, masked_account_number_last4)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertTrue(depositSummary.getAccountNumber().endsWith(masked_account_number_last4));
        }
    }

    @Test
    public void reportFindDeposits_FilterBy_SystemMid() throws ApiException {
        String merchantId = "101023947262";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertEquals(merchantId, depositSummary.getMerchantNumber());
        }
    }

    @Test
    public void reportFindDeposits_FilterBy_SystemHierarchy() throws ApiException {
        String hierarchy = "055-70-024-011-019";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits) {
            assertEquals(hierarchy, depositSummary.getMerchantHierarchy());
        }
    }

    @Test
    public void reportFindDeposits_FilterBy_WrongSystemMid() throws ApiException {
        String merchantId = "100000000000";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        assertEquals(0, deposits.size());
    }

    @Test
    public void reportFindDeposits_FilterBy_WrongSystemHierarchy() throws ApiException {
        String hierarchy = "000-70-024-000-000";

        DepositSummaryList deposits =
                ReportingService
                        .findDeposits()
                        .where(SearchCriteria.StartDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute("GpApiConfig");

        assertNotNull(deposits);
        assertEquals(0, deposits.size());
    }

    @Test
    public void reportFindDeposits_FilterBy_RandomUUIDSystemHierarchy() throws ApiException {
        try {
            ReportingService
                    .findDeposits()
                    .where(SearchCriteria.StartDate, DATE_2020_01_01)
                    .and(DataServiceCriteria.SystemHierarchy, UUID.randomUUID().toString())
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40105", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - system.hierarchy", ex.getMessage());
        }
    }

    @Ignore // Although documentation indicates from_time_created is required, the real endpoint returns results.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindDeposits_WithoutFromTimeCreated() throws ApiException {
        try {
            ReportingService
                    .findDeposits()
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

}