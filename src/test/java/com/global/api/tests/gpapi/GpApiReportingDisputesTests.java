package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.DisputeSummaryList;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.enums.AdjustmentFunding;
import com.global.api.entities.enums.DisputeSortProperty;
import com.global.api.entities.enums.DisputeStage;
import com.global.api.entities.enums.DisputeStatus;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.global.api.gateways.GpApiConnector.DATE_SDF;
import static org.junit.Assert.*;

public class GpApiReportingDisputesTests {

    private static Date DATE_2020_01_01 = null;

    static {
        try {
            DATE_2020_01_01 = DATE_SDF.parse("2020-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public GpApiReportingDisputesTests() throws ApiException {

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
    // Disputes
    // ================================================================================

    @Test
    public void reportDisputeDetail() throws ApiException {
        String disputeId = "DIS_SAND_abcd1234";

        DisputeSummary response =
                ReportingService
                        .disputeDetail(disputeId)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(disputeId, response.getCaseId());
    }

    @Ignore
    // Requests to /disputes which returns 200 are not compressed results so our code avoid decompressing them,
    // Requests to /disputes which returns errors are compressed, but our code avoid decompressing them so we cannot parse the error
    // TODO: Reported error to GP-API team. Enable it when fixed.
    @Test
    public void reportDisputeDetail_WrongId() throws ApiException {
        String disputeId = "DIS_SAND_abcd123a";

        try {
            ReportingService
                    .disputeDetail(disputeId)
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

    @Test
    public void reportFindDisputes_OrderBy_Id() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);

        DisputeSummaryList disputesAscending =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAscending);

        assertNotSame(disputes, disputesAscending);
    }

    @Test
    public void reportFindDisputes_OrderBy_ARN() throws ApiException, ParseException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.ARN, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.EndStageDate, DATE_SDF.parse("2020-06-22"))
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertTrue(DateUtils.isAfterOrEquals(disputeSummary.getDepositDate(), DATE_2020_01_01));
        }
    }

    @Test
    public void reportFindDisputes_OrderBy_Brand() throws ApiException {
        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_Status() throws ApiException {
        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Status, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void ReportFindDisputes_OrderBy_Stage() throws ApiException {
        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_FromStageTimeCreated() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_ToStageTimeCreated() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_AdjustmentFunding() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_FromAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_ToAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_FilterBy_ARN() throws ApiException {
        String arn = "135091790340196";

        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertEquals(arn, disputeSummary.getTransactionARN());
        }
    }

    @Ignore
    // Requests to /disputes which returns 200 are not compressed results so our code avoid decompressing them,
    // Requests to /disputes which returns errors are compressed, but our code avoid decompressing them so we cannot parse the error
    // TODO: Reported error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindDisputes_FilterBy_ARN_NotFound() throws ApiException {
        String arn = "745000100375912";

        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Test
    public void reportFindDisputes_FilterBy_Brand_OrderBy_Brand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DISCOVER"};

        for (String cardBrand : cardBrands) {
            DisputeSummaryList disputes =
                    ReportingService
                            .findDisputes()
                            .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.CardBrand, cardBrand)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                String transactionCardType = disputeSummary.getTransactionCardType();
                if (transactionCardType != null) {
                    assertEquals(cardBrand, transactionCardType);
                }
            }
        }
    }

    @Ignore
    // Although requests are done with &status set properly, the real endpoint returns disputes with other statuses.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindDisputes_FilterBy_Status() throws ApiException {
        for (DisputeStatus disputeStatus : DisputeStatus.values()) {
            DisputeSummaryList disputes =
                    ReportingService
                            .findDisputes()
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.DisputeStatus, disputeStatus)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                assertEquals(disputeStatus.getValue(), disputeSummary.getCaseStatus());
            }
        }
    }

    @Test
    public void reportFindDisputes_FilterBy_Stage_OrderBy_Stage() throws ApiException {
        for (DisputeStage disputeStage : DisputeStage.values()) {
            // Although documentation allows a GOODFAITH value for &stage request param, the real endpoint does not.
            // TODO: Report error to GP-API team. Enable it when fixed.
            if (DisputeStage.Goodfaith.equals(disputeStage))
                continue;

            DisputeSummaryList disputes =
                    ReportingService
                            .findDisputes()
                            .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.DisputeStage, disputeStage)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
            }
        }
    }

    @Test
    public void reportFindDisputes_FilterBy_From_And_To_Stage_Time_Created() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.EndStageDate, new Date())
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_FilterBy_From_And_To_Adjustment_Time_Created() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartAdjustmentDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.EndAdjustmentDate, new Date())
                        .and(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_FilterBy_AdjustmentFunding() throws ApiException {
        AdjustmentFunding adjustmentFunding = AdjustmentFunding.Debit;

        DisputeSummaryList disputesDebit =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.AdjustmentFunding, adjustmentFunding)
                        .execute("GpApiConfig");

        assertNotNull(disputesDebit);

        AdjustmentFunding adjustmentFundingCredit = AdjustmentFunding.Credit;
        DisputeSummaryList disputesCredit =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.AdjustmentFunding, adjustmentFundingCredit)
                        .execute("GpApiConfig");

        assertNotNull(disputesCredit);

        assertNotSame(disputesDebit, disputesCredit);
    }

    @Test
    public void reportFindDisputes_FilterBy_MerchantId_And_SystemHierarchy() throws ApiException {
        String merchantId = "8593872";
        String systemHierarchy = "111-23-099-002-005";

        List<DisputeSummary> disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertEquals(merchantId, disputeSummary.getCaseMerchantId());
            assertEquals(systemHierarchy, disputeSummary.getMerchantHierarchy());
        }
    }

    @Test
    public void reportFindDisputes_FilterBy_WrongMerchantId() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.MerchantId, UUID.randomUUID().toString())
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Test
    public void reportFindDisputes_FilterBy_WrongHierarchy() throws ApiException {
        String hierarchy = "111-23-099-001-009";
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Test
    public void reportFindDisputes_Without_FromStageTimeCreated() throws ApiException {
        try {
            ReportingService
                    .findDisputes()
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40074", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("Status Code: 400 - Retrieving a List of Disputes expects the 'from_stage_time_created' populated", ex.getMessage());
        }
    }

    @Test
    public void reportFindDisputes_OrderBy_Id_With_Brand_VISA() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.CardBrand, "VISA")
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_Id_With_Status_UnderReview() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.DisputeStatus, DisputeStatus.UnderReview)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindDisputes_OrderBy_Id_With_Stage_Chargeback() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.DisputeStage, DisputeStage.Chargeback)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    // ================================================================================
    // Settlement disputes
    // ================================================================================
    @Ignore // TODO: Find issue and fix
    @Test
    public void reportSettlementDisputeDetail() throws ApiException {
        String settlementDisputeId = "DIS_810";

        DisputeSummary response =
                ReportingService
                        .settlementDisputeDetail(settlementDisputeId)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(settlementDisputeId, response.getCaseId());
    }

    @Test
    public void reportSettlementDisputeDetail_WrongID() throws ApiException {
        String settlementDisputeId = UUID.randomUUID().toString();

        try {
            ReportingService
                    .settlementDisputeDetail(settlementDisputeId)
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Disputes " + settlementDisputeId + " not found at " +
                    "this /ucp/settlement/disputes/" + settlementDisputeId + "?account_name=Settlement%20Reporting", ex.getMessage());
        }
    }

    //TODO - status = funded and "to_stage_time_created": "2020-12-17T23:59:59.999Z" added as default for filter
    @Test
    public void reportFindSettlementDisputes_OrderBy_Id() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindSettlementDisputes_OrderBy_ARN() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.ARN, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindSettlementDisputes_OrderBy_Id_With_Status_UnderReview() throws ApiException {
        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.DisputeStatus, DisputeStatus.UnderReview)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindSettlementDisputes_OrderBy_Brand() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    public void reportFindSettlementDisputes_OrderBy_Status() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Status, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Status, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    public void reportFindSettlementDisputes_OrderBy_Stage() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    public void reportFindSettlementDisputes_OrderBy_FromStageTimeCreated() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    public void reportFindSettlementDisputes_OrderBy_ToStageTimeCreated() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    public void reportFindSettlementDisputes_OrderBy_AdjustmentFunding() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    public void reportFindSettlementDisputes_OrderBy_FromAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    public void reportFindSettlementDisputes_OrderBy_ToAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryList disputesAsc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Ascending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesAsc);

        DisputeSummaryList disputesDesc =
                ReportingService
                        .findSettlementDisputes()
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Descending)
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .execute("GpApiConfig");

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_ARN() throws ApiException {
        String arn = "74500010037624410827759";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertEquals(arn, disputeSummary.getTransactionARN());
        }
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_ARN_NotFound() throws ApiException {
        String arn = "135091790340196";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_Brand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DISCOVER"};

        for (String cardBrand : cardBrands) {

            DisputeSummaryList disputes =
                    ReportingService
                            .findSettlementDisputes()
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.CardBrand, cardBrand)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                String transactionCardType = disputeSummary.getTransactionCardType();
                if (transactionCardType != null) {
                    assertEquals(cardBrand, transactionCardType);
                }
            }
        }
    }

    //TODO - Brand = JCB results are returned even JCB is not in the agreed list
    @Test
    public void reportFindSettlementDisputes_FilterBy_Brand_NotFound() throws ApiException {
        String cardBrand = "Bank of America";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.CardBrand, cardBrand)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Ignore
    // Although requests are done with &STATUS set properly, the real endpoint returns disputes with other statuses.
    // Apart from this, the request param: STATUS is accepted, but status is not.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void reportFindSettlementDisputes_FilterBy_Status() throws ApiException {
        for (DisputeStatus disputeStatus : DisputeStatus.values()) {
            DisputeSummaryList disputes =
                    ReportingService
                            .findSettlementDisputes()
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.DisputeStatus, disputeStatus)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                assertEquals(disputeStatus.getValue(), disputeSummary.getCaseStatus());
            }
        }
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_AllStages() throws ApiException {
        for (DisputeStage disputeStage : DisputeStage.values()) {

            DisputeSummaryList disputes =
                    ReportingService
                            .findSettlementDisputes()
                            .withPaging(1, 10)
                            .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                            .and(SearchCriteria.DisputeStage, disputeStage)
                            .execute("GpApiConfig");

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes) {
                assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
            }
        }
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_Stage() throws ApiException {
        DisputeStage disputeStage = DisputeStage.Chargeback;

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.DisputeStage, disputeStage)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
        }

        DisputeStage disputeStageReversal = DisputeStage.Reversal;

        DisputeSummaryList disputesReversal =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, DATE_2020_01_01)
                        .and(SearchCriteria.DisputeStage, disputeStageReversal)
                        .execute("GpApiConfig");

        assertNotNull(disputesReversal);
        for (DisputeSummary disputeSummary : disputesReversal) {
            assertEquals(disputeStageReversal.getValue(), disputeSummary.getCaseStage());
        }

        assertNotSame(disputes, disputesReversal);
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_FromAndToStageTimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -40);
        Date endDate = DateUtils.addDays(new Date(), -20);

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.EndStageDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertTrue(DateUtils.isAfterOrEquals(disputeSummary.getCaseTime(), startDate));
        }
    }

    //TODO - list not filtered by AdjustmentFunding which is not set on filter
    @Test
    public void reportFindSettlementDisputes_FilterBy_AdjustmentFunding() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -10);
        AdjustmentFunding adjustmentFunding = AdjustmentFunding.Credit;

        DisputeSummaryList disputesCredit =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.AdjustmentFunding, adjustmentFunding)
                        .execute("GpApiConfig");

        assertNotNull(disputesCredit);

        AdjustmentFunding adjustmentFundingDebit = AdjustmentFunding.Debit;

        DisputeSummaryList disputesDebit =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.AdjustmentFunding, adjustmentFundingDebit)
                        .execute("GpApiConfig");

        assertNotNull(disputesDebit);

        assertNotSame(disputesCredit, disputesDebit);
    }

    //TODO - from_adjustment_time_created and to_adjustment_time_created not set on filter
    @Test
    public void reportFindSettlementDisputes_FilterBy_FromAndToAdjustmentTimeCreated() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -10);
        Date endDate = DateUtils.addDays(new Date(), -5);

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.StartAdjustmentDate, startDate)
                        .and(DataServiceCriteria.EndAdjustmentDate, endDate)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_SystemMidAndHierarchy() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -10);
        String merchantId = "101023947262";
        String systemHierarchy = "055-70-024-011-019";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes) {
            assertEquals(merchantId, disputeSummary.getCaseMerchantId());
            assertEquals(systemHierarchy, disputeSummary.getMerchantHierarchy());
        }
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_WrongSystemMid() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -10);
        String merchantId = "8593872";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

    @Test
    public void reportFindSettlementDisputes_FilterBy_WrongSystemHierarchy() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -10);
        String systemHierarchy = "111-23-099-002-005";

        DisputeSummaryList disputes =
                ReportingService
                        .findSettlementDisputes()
                        .withPaging(1, 10)
                        .where(DataServiceCriteria.StartStageDate, startDate)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute("GpApiConfig");

        assertNotNull(disputes);
        assertEquals(0, disputes.size());
    }

}