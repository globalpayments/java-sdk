package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.enums.StoredPaymentMethodSortProperty;
import com.global.api.entities.enums.StoredPaymentMethodStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.StoredPaymentMethodSummary;
import com.global.api.entities.reporting.StoredPaymentMethodSummaryPaged;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;
import static org.junit.Assert.*;

public class GpApiReportingStoredPaymentMethodsTests extends BaseGpApiReportingTest {

    private static String token;
    private static CreditCardData card;

    public GpApiReportingStoredPaymentMethodsTests() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);
        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        try {
            token = card.tokenize(GP_API_CONFIG_NAME);
            assertFalse("Token could not be generated.", StringUtils.isNullOrEmpty(token));
        } catch (GatewayException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void ReportStoredPaymentMethodDetail() throws ApiException {
        StoredPaymentMethodSummary response =
                ReportingService
                        .storedPaymentMethodDetail(token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(token, response.getId());
    }

    @Test
    public void ReportStoredPaymentMethodDetailWithNonExistentId() throws ApiException {
        String storedPaymentMethodId = "PMT_" + UUID.randomUUID();

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .storedPaymentMethodDetail(storedPaymentMethodId)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40118", ex.getResponseText());
            assertEquals("Status Code: 404 - PAYMENT_METHODS " + storedPaymentMethodId + " not found at this /ucp/payment-methods/" + storedPaymentMethodId, ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportStoredPaymentMethodDetailWithRandomId() throws ApiException {
        String storedPaymentMethodId = UUID.randomUUID().toString().replace("-", "");

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .storedPaymentMethodDetail(storedPaymentMethodId)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - payment_method.id: " + storedPaymentMethodId + " contains unexpected data", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportStoredPaymentMethodDetail_WithNullId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .storedPaymentMethodDetail(null)
                    .execute(GP_API_CONFIG_NAME);
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("storedPaymentMethodId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_Id() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StoredPaymentMethodId, token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(token, storedPaymentMethodSummary.getId());
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_RandomId() throws ApiException {
        String storedPaymentMethodId = "PMT_" + UUID.randomUUID();

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StoredPaymentMethodId, storedPaymentMethodId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Ignore
    // TODO: Reported the the GP API team. Enable when fixed.
    // Endpoint is retrieving not filtered results
    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_NumberLast4() throws ApiException {
        String numberLast4 = card.getNumber().substring(card.getNumber().length() - 4);

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.CardNumberLastFour, numberLast4)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals("xxxxxxxxxxxx" + numberLast4, storedPaymentMethodSummary.getCardLast4());
        }
    }

    @Ignore
    // TODO: Reported the the GP API team. Enable when fixed.
    // Endpoint is retrieving not filtered results
    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_NumberLast4_Set0000() throws ApiException {
        String numberLast4 = "0000";

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.CardNumberLastFour, numberLast4)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_Reference() throws ApiException {
        StoredPaymentMethodSummary response =
                ReportingService
                        .storedPaymentMethodDetail(token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response.getReference());

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.ReferenceNumber, response.getReference())
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(response.getReference(), storedPaymentMethodSummary.getReference());
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_Status() throws ApiException {
        StoredPaymentMethodStatus status = StoredPaymentMethodStatus.Active;

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StoredPaymentMethodStatus, status)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(getValueIfNotNull(status), storedPaymentMethodSummary.getStatus());
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_StartDate_And_EndDate() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_LAST_MONTH_DATE)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(storedPaymentMethodSummary.getTimeCreated().toDate(), REPORTING_START_DATE));
            assertTrue(DateUtils.isBeforeOrEquals(storedPaymentMethodSummary.getTimeCreated().toDate(), REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_StartDate_And_EndDate_CurrentDay() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.StartDate, REPORTING_START_DATE)
                        .and(SearchCriteria.EndDate, REPORTING_END_DATE)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(storedPaymentMethodSummary.getTimeCreated().toDate(), REPORTING_START_DATE));
            assertTrue(DateUtils.isBeforeOrEquals(storedPaymentMethodSummary.getTimeCreated().toDate(), REPORTING_END_DATE));
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_By_StartLastUpdatedDate_And_EndLastUpdatedDate() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartLastUpdatedDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndLastUpdatedDate, REPORTING_LAST_MONTH_DATE)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        // TODO: There is no way to validate the response data
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_OrderBy_TimeCreated_Ascending() throws ApiException {
        StoredPaymentMethodSummaryPaged storedPaymentMethodSummaryAscending =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(storedPaymentMethodSummaryAscending.getResults());

        List<StoredPaymentMethodSummary> results = storedPaymentMethodSummaryAscending.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            Date current = results.get(i).getTimeCreated().toDate();
            Date next = results.get(i + 1).getTimeCreated().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(current, next));
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_OrderBy_TimeCreated_Descending() throws ApiException {
        StoredPaymentMethodSummaryPaged storedPaymentMethodSummaryDescending =
                ReportingService
                        .findStoredPaymentMethodsPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(storedPaymentMethodSummaryDescending.getResults());

        List<StoredPaymentMethodSummary> results = storedPaymentMethodSummaryDescending.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            Date current = results.get(i).getTimeCreated().toDate();
            Date next = results.get(i + 1).getTimeCreated().toDate();
            assertTrue(DateUtils.isAfterOrEquals(current, next));
        }
    }

}