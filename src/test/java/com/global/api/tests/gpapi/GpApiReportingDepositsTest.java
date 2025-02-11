package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.DepositSortProperty;
import com.global.api.entities.enums.DepositStatus;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DepositSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiReportingDepositsTest extends BaseGpApiReportingTest {

    public GpApiReportingDepositsTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, null);
        ServicesContainer.configureService(config);
    }

    @Test
    @Order(1)
    public void ReportDepositDetail() throws ApiException {
        String depositId = "DEP_2342423423";

        DepositSummary deposit =
                ReportingService
                        .depositDetail(depositId)
                        .execute();

        assertNotNull(deposit);
        assertEquals(depositId, deposit.getDepositId());
    }

    @Test
    @Order(2)
    public void ReportDepositDetail_WrongId() throws ApiException {
        String depositId = "DEP_234242342";
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .depositDetail(depositId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Deposits DEP_234242342 not found at this /ucp/settlement/deposits/DEP_234242342", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(3)
    public void ReportFindDepositsWithCriteria() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), REPORTING_START_DATE));
        }
    }

    @Test
    @Order(4)
    public void ReportFindDepositsPaged_FilterBy_StartDate_OrderBy_TimeCreated() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), REPORTING_START_DATE));
        }
    }

    @Test
    @Order(5)
    public void ReportFindDepositsPaged_OrderBy_DepositId() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.DepositId, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), REPORTING_START_DATE));
        }
    }

    @Test
    @Order(6)
    public void ReportFindDepositsPaged_OrderBy_Status() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.Status, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), REPORTING_START_DATE));
        }
    }

    @Test
    @Order(7)
    public void ReportFindDepositsPaged_OrderBy_Type() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.Type, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(depositSummary.getDepositDate(), REPORTING_START_DATE));
        }

        DepositSummaryPaged depositsAsc =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.Type, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(depositsAsc);

        assertNotSame(deposits, depositsAsc);
    }

    @Test
    @Order(8)
    public void CompareResults_ReportFindDepositsPaged_OrderBy_DepositId_And_Type() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.DepositId, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();
        assertNotNull(deposits);

        DepositSummaryPaged depositsType =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DepositSortProperty.Type, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();
        assertNotNull(depositsType);

        assertNotSame(deposits, depositsType);
    }

    @Test
    @Order(9)
    public void ReportFindDepositsPaged_FilterBy_DepositReference() throws ApiException {
        String depositReference = "DEP_2342423423";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withDepositReference(depositReference)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();
        assertNotNull(deposits);
        assertEquals(1, deposits.getResults().size());
        assertEquals(depositReference, deposits.getResults().get(0).getDepositId());
    }

    @Test
    @Order(10)
    public void ReportFindDepositsPaged_FilterBy_WrongDepositReference() throws ApiException {
        String depositReference = UUID.randomUUID().toString().replace("-", "");

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .withDepositReference(depositReference)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .execute();
        assertNotNull(deposits);
        assertEquals(0, deposits.getResults().size());
    }

    @Test
    @Order(11)
    public void ReportFindDepositsPaged_FilterBy_Status() throws ApiException {
        for (DepositStatus depositStatus : DepositStatus.values()) {
            DepositSummaryPaged deposits =
                    ReportingService
                            .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                            .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                            .and(SearchCriteria.DepositStatus, depositStatus)
                            .execute();
            assertNotNull(deposits);
            for (DepositSummary depositSummary : deposits.getResults()) {
                assertEquals(depositStatus.toString().toUpperCase().trim().replace("IRREGULAR", "IRREG"),
                        depositSummary.getStatus().trim().replace(" ", ""));
            }
        }
    }

    //TODO - empty list returned
    @Test
    @Order(12)
    public void ReportFindDepositsPaged_FilterBy_StartAndEndDate() throws ApiException {
        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_LAST_MONTH_DATE)
                        .execute();
        assertNotNull(deposits);
    }

    @Test
    @Order(13)
    public void ReportFindDepositsPaged_FilterBy_Amount() throws ApiException {
        BigDecimal amount = new BigDecimal("114");

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();
        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertEquals(amount, depositSummary.getAmount());
        }
    }

    @Test
    @Order(14)
    public void ReportFindDepositsPaged_FilterBy_NotFoundAmount() throws ApiException {
        BigDecimal amount = new BigDecimal("1");

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();

        assertNotNull(deposits);
        assertEquals(0, deposits.getResults().size());
    }

    @Test
    @Order(15)
    public void ReportFindDepositsPaged_FilterBy_MaskedAccountNumberLast4() throws ApiException {
        String masked_account_number_last4 = "9999";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.AccountNumberLastFour, masked_account_number_last4)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertTrue(depositSummary.getAccountNumber().endsWith(masked_account_number_last4));
        }
    }

    @Test
    @Order(16)
    public void ReportFindDepositsPaged_FilterBy_SystemMerchantId() throws ApiException {
        String merchantId = "101023947262";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertEquals(merchantId, depositSummary.getMerchantNumber());
        }
    }

    @Test
    @Order(17)
    public void ReportFindDepositsPaged_FilterBy_SystemHierarchy() throws ApiException {
        String hierarchy = "055-70-024-011-019";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute();

        assertNotNull(deposits);
        for (DepositSummary depositSummary : deposits.getResults()) {
            assertEquals(hierarchy, depositSummary.getMerchantHierarchy());
        }
    }

    @Test
    @Order(18)
    public void ReportFindDepositsPaged_FilterBy_WrongSystemMerchantId() throws ApiException {
        String merchantId = "100000000000";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute();

        assertNotNull(deposits);
        assertEquals(0, deposits.getResults().size());
    }

    @Test
    @Order(19)
    public void ReportFindDepositsPaged_FilterBy_WrongSystemHierarchy() throws ApiException {
        String hierarchy = "000-70-024-000-000";

        DepositSummaryPaged deposits =
                ReportingService
                        .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute();

        assertNotNull(deposits);
        assertEquals(0, deposits.getResults().size());
    }

    @Test
    @Order(20)
    public void ReportFindDepositsPaged_FilterBy_RandomUUIDSystemHierarchy() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                    .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                    .and(DataServiceCriteria.SystemHierarchy, UUID.randomUUID().toString())
                    .execute();

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40105", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Invalid Value provided in the input field - system.hierarchy", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Disabled // Although documentation indicates from_time_created is required, the real endpoint returns results.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    @Order(21)
    public void ReportFindDepositsPaged_WithoutFromTimeCreated() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .findDepositsPaged(FIRST_PAGE, PAGE_SIZE)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

}
