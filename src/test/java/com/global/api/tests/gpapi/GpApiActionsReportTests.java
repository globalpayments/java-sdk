package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.ActionSortProperty;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.ActionSummary;
import com.global.api.entities.reporting.ActionSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import com.global.api.utils.StringUtils;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiActionsReportTests extends BaseGpApiTest {

    private final ActionSummary sampleAction;

    public GpApiActionsReportTests() throws ApiException, ParseException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);


        sampleAction = ReportingService
                .findActionsPaged(1, 1)
                .where(SearchCriteria.StartDate, DateUtils.addDays(DateTime.now().toDate(), -5))
                .execute(GP_API_CONFIG_NAME).getResults().get(0);
    }

    @Test
    public void ReportActionDetail() throws ApiException {
        String actionId = sampleAction.getId();

        ActionSummary response =
                ReportingService
                        .actionDetail(actionId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(actionId, response.getId());
    }

    @Test
    public void ReportActionDetailWithRandomId() throws ApiException {
        String actionId = "ACT_" + UUID.randomUUID();
        boolean exceptionCaught = false;

        try {
            ReportingService
                    .actionDetail(actionId)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40118", ex.getResponseText());
            assertEquals("Status Code: 404 - Actions " + actionId + " not found at this /ucp/actions/" + actionId, ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindActionsPaged_By_Id() throws ApiException {
        String actionId = sampleAction.getId();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ActionId, actionId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(actionId,el.getId());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_Random_Id() throws ApiException {
        String actionId = "ACT_" + UUID.randomUUID();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 5)
                        .where(SearchCriteria.ActionId, actionId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Test
    public void ReportFindActionsPaged_By_Type() throws ApiException {
        String actionType = sampleAction.getType();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ActionType, actionType)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(actionType, el.getType());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_RandomType() throws ApiException {
        final String actionType = "USERS";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ActionType, actionType)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        assertEquals(0, result.getTotalRecordCount());
    }

    @Test
    public void ReportFindActionsPaged_By_Resource() throws ApiException {
        String resource = sampleAction.getResource();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.Resource, resource)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(resource, el.getResource());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_ResourceStatus() throws ApiException {
        String resourceStatus = !StringUtils.isNullOrEmpty(sampleAction.getResourceStatus()) ? sampleAction.getResourceStatus() : "AVAILABLE";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ResourceStatus, resourceStatus)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(resourceStatus, el.getResourceStatus());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_ResourceId() throws ApiException {
        String resourceId = sampleAction.getResourceId();

        if(StringUtils.isNullOrEmpty(resourceId)) {
            List<ActionSummary> results = ReportingService
                    .findActionsPaged(1, 25)
                    .where(SearchCriteria.StartDate, DateUtils.addDays(DateTime.now().toDate(), -5))
                    .execute(GP_API_CONFIG_NAME).getResults();

            for(ActionSummary actionSummary : results) {
                resourceId = actionSummary.getResourceId();
                if( !StringUtils.isNullOrEmpty(resourceId)) {
                    resourceId = actionSummary.getResourceId();
                    break;
                }
            }
        }

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ResourceId, resourceId)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(resourceId, el.getResourceId());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_StartDate_And_EndDate() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -30);
        Date endDate = DateUtils.addDays(new Date(), -10);

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            Date transactionDate = el.getTimeCreated().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(transactionDate, endDate));
            assertTrue(DateUtils.isAfterOrEquals(transactionDate, startDate));
        }
    }

    @Ignore
    @Test
    public void ReportFindActionsPaged_By_RandomMerchantName() throws ApiException {
        String merchantName = "Sandbox_merchant_" + UUID.randomUUID();
        boolean exceptionCaught = false;

        try {
            ReportingService
                    .findActionsPaged(1, 25)
                    .where(SearchCriteria.MerchantName, merchantName)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("40003", ex.getResponseText());
            assertEquals("Status Code: Forbidden - Token does not match merchant_name in the request", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportFindActionsPaged_By_MerchantName() throws ApiException {
        String merchantName = sampleAction.getMerchantName();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.MerchantName, merchantName)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(merchantName, el.getMerchantName());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_AccountName() throws ApiException {
        String accountName = !StringUtils.isNullOrEmpty(sampleAction.getAccountName()) ? sampleAction.getAccountName() : "Tokenization";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.AccountName, accountName)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(accountName, el.getAccountName());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_AppName() throws ApiException {
        String appName = sampleAction.getAppName();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.AppName, appName)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(appName, el.getAppName());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_Version() throws ApiException {
        String version = sampleAction.getVersion();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.Version, version)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(version, el.getVersion());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_WrongVersion() throws ApiException {
        final String version = "2020-05-10";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.Version, version)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(version, el.getVersion());
        }
        assertEquals(0, result.getTotalRecordCount());
    }

    @Test
    public void ReportFindActionsPaged_By_ResponseCode() throws ApiException {
		String responseCode = !StringUtils.isNullOrEmpty(sampleAction.getResponseCode()) ? sampleAction.getResponseCode() : "SUCCESS";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ResponseCode, responseCode)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for(ActionSummary el : result.getResults()) {
            assertEquals(responseCode, el.getResponseCode());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_ResponseCode_Declined() throws ApiException {
        final String responseCode = "DECLINED";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ResponseCode, responseCode)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for(ActionSummary el : result.getResults()) {
            assertEquals(responseCode, el.getResponseCode());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_HttpResponseCode() throws ApiException {
        String httpResponseCode = sampleAction.getHttpResponseCode();

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.HttpResponseCode, httpResponseCode)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(httpResponseCode, el.getHttpResponseCode());
        }
    }

    @Test
    public void ReportFindActionsPaged_By_502_HttpResponseCode() throws ApiException {
        final String httpResponseCode = "502";

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.HttpResponseCode, httpResponseCode)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(result.getResults());
        for (ActionSummary el : result.getResults()) {
            assertEquals(httpResponseCode, el.getHttpResponseCode());
        }
    }

    @Test
    public void ReportFindActionsPaged_OrderBy_TimeCreated_Ascending() throws ApiException {
        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .orderBy(ActionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);

        List<ActionSummary> results = result.getResults();
        assertNotNull(results);

        for (int i = 0; i < results.size() - 1; i++) {
            ActionSummary current = results.get(i);
            ActionSummary next = results.get(i + 1);
            assertTrue(DateUtils.isBeforeOrEquals(current.getTimeCreated().toDate(), next.getTimeCreated().toDate()));
        }
    }

    @Test
    public void ReportFindActionsPaged_OrderBy_TimeCreated_Descending() throws ApiException {
        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .orderBy(ActionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);

        List<ActionSummary> results = result.getResults();
        assertNotNull(results);

        for (int i = 0; i < results.size() - 1; i++) {
            ActionSummary current = results.get(i);
            ActionSummary next = results.get(i + 1);
            assertTrue(DateUtils.isAfterOrEquals(current.getTimeCreated().toDate(), next.getTimeCreated().toDate()));
        }
    }

    @Test
    public void ReportFindActionsPaged_OrderBy_TimeCreated() throws ApiException {
        ActionSummaryPaged resultDesc =
                ReportingService
                        .findActionsPaged(1, 25)
                        .orderBy(ActionSortProperty.TimeCreated, SortDirection.Descending)
                        .execute(GP_API_CONFIG_NAME);

        List<ActionSummary> resultsDesc = resultDesc.getResults();
        assertNotNull(resultsDesc);
        for (int i = 0; i < resultsDesc.size() - 1; i++) {
            Date current = resultsDesc.get(i).getTimeCreated().toDate();
            Date next = resultsDesc.get(i + 1).getTimeCreated().toDate();
            assertTrue(DateUtils.isBeforeOrEquals(current, next));
        }

        ActionSummaryPaged resultAsc =
                ReportingService
                        .findActionsPaged(1, 25)
                        .orderBy(ActionSortProperty.TimeCreated, SortDirection.Ascending)
                        .execute(GP_API_CONFIG_NAME);

        List<ActionSummary> resultsAsc = resultAsc.getResults();

        assertNotNull(resultsAsc);
        for (int i = 0; i < resultsDesc.size() - 1; i++) {
            Date current = resultsDesc.get(i).getTimeCreated().toDate();
            Date next = resultsDesc.get(i + 1).getTimeCreated().toDate();
            assertTrue(DateUtils.isAfterOrEquals(current, next));
        }

        assertNotSame(resultAsc, resultDesc);
    }

    @Test
    public void ReportFindActionsPaged_By_MultipleFilters() throws ApiException {
        final String actionType = "AUTHORIZE";
        final String resource = "TRANSACTIONS";
        final String resourceStatus = "DECLINED";
        final String accountName = "Transaction_Processing";
        final String merchantName = sampleAction.getMerchantName();
        final String version = "2020-12-22";
        Date startDate = DateUtils.addDays(DateTime.now().toDate(), -30);
        Date endDate = DateUtils.addDays(DateTime.now().toDate(), -20);

        ActionSummaryPaged result =
                ReportingService
                        .findActionsPaged(1, 25)
                        .where(SearchCriteria.ActionType, actionType)
                        .and(SearchCriteria.Resource, resource)
                        .and(SearchCriteria.ResourceStatus, resourceStatus)
                        .and(SearchCriteria.AccountName, accountName)
                        .and(SearchCriteria.MerchantName, merchantName)
                        .and(SearchCriteria.Version, version)
                        .and(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        List<ActionSummary> results = result.getResults();
        assertNotNull(results);
        for(ActionSummary el :  results) {
            assertEquals(actionType, el.getType());
            assertEquals(resource, el.getResource());
            assertEquals(resourceStatus, el.getResourceStatus());
            assertEquals(accountName, el.getAccountName());
            assertEquals(merchantName, el.getMerchantName());
            assertEquals(version, el.getVersion());
        }
    }

}