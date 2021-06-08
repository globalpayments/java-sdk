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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.getValueIfNotNull;
import static org.junit.Assert.*;

public class GpApiReportingStoredPaymentMethodsTests extends BaseGpApiTest {

    private static String token;
    private static CreditCardData card;

    public GpApiReportingStoredPaymentMethodsTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg")
                .setAppKey("ockJr6pv6KFoGiZA");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        try {
            token = card.tokenize(GP_API_CONFIG_NAME);
            assertFalse("Token could not be generated.", StringUtils.isNullOrEmpty(token));
        } catch (GatewayException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void reportStoredPaymentMethodDetail() throws ApiException {
        StoredPaymentMethodSummary response =
                ReportingService
                        .storedPaymentMethodDetail(token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(token, response.getId());
    }

    @Test
    public void reportStoredPaymentMethodDetailWithNonExistentId() throws ApiException {
        String storedPaymentMethodId = "PMT_" + UUID.randomUUID();

        try {
            ReportingService
                    .storedPaymentMethodDetail(storedPaymentMethodId)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40118", ex.getResponseText());
            assertEquals("Status Code: 404 - PAYMENT_METHODS " + storedPaymentMethodId + " not found at this /ucp/payment-methods/" + storedPaymentMethodId, ex.getMessage());
        }
    }

    @Test
    public void reportStoredPaymentMethodDetailWithRandomId() throws ApiException {
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
    public void reportFindStoredPaymentMethodsPaged_By_Id() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(DataServiceCriteria.StoredPaymentMethodId, token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(token, storedPaymentMethodSummary.getId());
        }
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_RandomId() throws ApiException {
        String storedPaymentMethodId = "PMT_" + UUID.randomUUID();

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(DataServiceCriteria.StoredPaymentMethodId, storedPaymentMethodId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Ignore
    // TODO: Inform the the GP API team
    // Endpoint is retrieving not filtered results
    @Test
    public void reportFindStoredPaymentMethodsPaged_By_NumberLast4() throws ApiException {
        String numberLast4 = card.getNumber().substring(card.getNumber().length() - 4);

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(DataServiceCriteria.CardNumberLastFour, numberLast4)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals("xxxxxxxxxxxx" + numberLast4, storedPaymentMethodSummary.getCardLast4());
        }
    }

    @Ignore
    // TODO: Inform the the GP API team
    // Endpoint is retrieving not filtered results
    @Test
    public void reportFindStoredPaymentMethodsPaged_By_NumberLast4_0000() throws ApiException {
        String numberLast4 = "0000";

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(DataServiceCriteria.CardNumberLastFour, numberLast4)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_Reference() throws ApiException {
        StoredPaymentMethodSummary response =
                ReportingService
                        .storedPaymentMethodDetail(token)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response.getReference());

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(SearchCriteria.ReferenceNumber, response.getReference())
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(response.getReference(), storedPaymentMethodSummary.getReference());
        }
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_Status() throws ApiException {
        StoredPaymentMethodStatus status = StoredPaymentMethodStatus.Active;

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(SearchCriteria.StoredPaymentMethodStatus, status)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertEquals(getValueIfNotNull(status), storedPaymentMethodSummary.getStatus());
        }
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -10);

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(storedPaymentMethodSummary.getTimeCreated(), startDate));
            assertTrue(DateUtils.isBeforeOrEquals(storedPaymentMethodSummary.getTimeCreated(), endDate));
        }
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_StartDate_And_EndDate_CurrentDay() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -10);

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (StoredPaymentMethodSummary storedPaymentMethodSummary : result.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(storedPaymentMethodSummary.getTimeCreated(), startDate));
            assertTrue(DateUtils.isBeforeOrEquals(storedPaymentMethodSummary.getTimeCreated(), endDate));
        }
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_By_StartLastUpdatedDate_And_EndLastUpdatedDate() throws ApiException {
        Date startLastUpdatedDate = DateUtils.addDays(new Date(), -30);
        Date endLastUpdatedDate = DateUtils.addDays(new Date(), -10);

        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .where(DataServiceCriteria.StartLastUpdatedDate, startLastUpdatedDate)
                        .and(DataServiceCriteria.EndLastUpdatedDate, endLastUpdatedDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        // TODO: There is no way to validate the response data
    }

    @Test
    public void reportFindStoredPaymentMethodsPaged_OrderBy_TimeCreated_Ascending() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());

        List<StoredPaymentMethodSummary> results = result.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(DateUtils.isBeforeOrEquals(results.get(0).getTimeCreated(), results.get(i + 1).getTimeCreated()));
        }
    }

    @Test
    public void ReportFindStoredPaymentMethodsPaged_OrderBy_TimeCreated_Descending() throws ApiException {
        StoredPaymentMethodSummaryPaged result =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 25)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());

        List<StoredPaymentMethodSummary> results = result.getResults();
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(DateUtils.isAfterOrEquals(results.get(0).getTimeCreated(), results.get(i + 1).getTimeCreated()));
        }
    }

    @AfterClass
    public static void cleanup() throws BuilderException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
    }

}
