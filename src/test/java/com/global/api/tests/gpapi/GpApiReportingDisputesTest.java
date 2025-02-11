package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.enums.DisputeSortProperty;
import com.global.api.entities.enums.DisputeStage;
import com.global.api.entities.enums.DisputeStatus;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.DisputeSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.Random;
import java.util.UUID;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiReportingDisputesTest extends BaseGpApiReportingTest {

    public GpApiReportingDisputesTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, null);
        ServicesContainer.configureService(config);
    }

    @Test
    @Order(1)
    public void ReportDisputeDetail() throws ApiException {
        String disputeId = "DIS_SAND_abcd1234";

        DisputeSummary response =
                ReportingService
                        .disputeDetail(disputeId)
                        .execute();

        assertNotNull(response);
        assertEquals(disputeId, response.getCaseId());
    }

    @Test
    @Order(2)
    public void ReportDisputeDetail_WrongId() throws ApiException {
        String disputeId = "DIS_SAND_abcd123a";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .disputeDetail(disputeId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40073", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - 101,Unable to locate dispute record for that ID. Please recheck the ID provided.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(3)
    public void ReportFindDisputesPaged_OrderBy_Id() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);

        DisputeSummaryPaged disputesAscending =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAscending);

        assertNotSame(disputes, disputesAscending);
    }

    @Test
    @Order(4)
    public void ReportFindDisputesPaged_OrderBy_ARN() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ARN, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndStageDate, REPORTING_LAST_MONTH_DATE)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(disputeSummary.getDepositDate(), REPORTING_START_DATE));
            assertTrue(DateUtils.isBeforeOrEquals(disputeSummary.getDepositDate(), REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    @Order(5)
    public void ReportFindDisputesPaged_OrderBy_Brand() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(6)
    public void ReportFindDisputesPaged_OrderBy_Status() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Status, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(7)
    public void ReportFindDisputesPaged_OrderBy_Stage() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Disabled
    // orderBy FromStageTimeCreated is not supported anymore
    @Test
    @Order(8)
    public void ReportFindDisputesPaged_OrderBy_FromStageTimeCreated() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(9)
    public void ReportFindDisputesPaged_OrderBy_ToStageTimeCreated() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(10)
    public void ReportFindDisputesPaged_OrderBy_AdjustmentFunding() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(11)
    public void ReportFindDisputesPaged_OrderBy_FromAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(12)
    public void ReportFindDisputesPaged_OrderBy_ToAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(13)
    public void ReportFindDisputesPaged_FilterBy_ARN() throws ApiException {
        String arn = "135091790340196";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(arn, disputeSummary.getTransactionARN());
        }
    }

    @Disabled
    // Requests to /disputes which returns 200 are not compressed results so our code avoid decompressing them,
    // Requests to /disputes which returns errors are compressed, but our code avoid decompressing them so we cannot parse the error
    // TODO: Reported error to GP-API team. Enable it when fixed.
    @Test
    @Order(14)
    public void ReportFindDisputesPaged_FilterBy_ARN_NotFound() throws ApiException {
        String arn = "745000100375912";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getPageSize());
    }

    @Test
    @Order(15)
    public void ReportFindDisputesPaged_FilterBy_Brand_OrderBy_Brand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DISCOVER"};

        for (String cardBrand : cardBrands) {
            DisputeSummaryPaged disputes =
                    ReportingService
                            .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                            .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                            .and(SearchCriteria.CardBrand, cardBrand)
                            .execute();

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes.getResults()) {
                String transactionCardType = disputeSummary.getTransactionCardType();
                if (transactionCardType != null) {
                    assertEquals(cardBrand, transactionCardType);
                }
            }
        }
    }

    @Test
    @Order(16)
    public void ReportFindDisputesPaged_FilterBy_Status() throws ApiException {
        for (DisputeStatus disputeStatus : DisputeStatus.values()) {
            DisputeSummaryPaged disputes =
                    ReportingService
                            .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                            .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                            .and(SearchCriteria.DisputeStatus, disputeStatus)
                            .execute();

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes.getResults()) {
                assertEquals(disputeStatus.getValue(), disputeSummary.getCaseStatus());
            }
        }
    }

    @Test
    @Order(17)
    public void ReportFindDisputesPaged_FilterBy_Stage_OrderBy_Stage() throws ApiException {
        for (DisputeStage disputeStage : DisputeStage.values()) {
            // Although documentation allows a GOODFAITH value for &stage request param, the real endpoint does not.
            // TODO: Report error to GP-API team. Enable it when fixed.
            if (DisputeStage.Goodfaith.equals(disputeStage))
                continue;

            DisputeSummaryPaged disputes =
                    ReportingService
                            .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                            .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                            .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                            .and(SearchCriteria.DisputeStage, disputeStage)
                            .execute();

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes.getResults()) {
                assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
            }
        }
    }

    @Test
    @Order(18)
    public void ReportFindDisputesPaged_FilterBy_From_And_To_Stage_Time_Created() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndStageDate, new Date())
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(19)
    public void ReportFindDisputesPaged_FilterBy_MerchantId_And_SystemHierarchy() throws ApiException {
        String merchantId = "8593872";
        String systemHierarchy = "111-23-099-002-005";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(merchantId, disputeSummary.getCaseMerchantId());
            assertEquals(systemHierarchy, disputeSummary.getMerchantHierarchy());
        }
    }

    @Test
    @Order(20)
    public void ReportFindDisputesPaged_FilterBy_WrongMerchantId() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, Integer.toString(new Random().nextInt(999999999)))
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(21)
    public void ReportFindDisputesPaged_FilterBy_WrongHierarchy() throws ApiException {
        String hierarchy = "111-23-099-001-009";
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.SystemHierarchy, hierarchy)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(22)
    public void ReportFindDisputesPaged_Without_FromStageTimeCreated() throws ApiException {
        try {
            ReportingService
                    .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                    .execute();

        } catch (GatewayException ex) {
            assertEquals("40074", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("Error occurred while communicating with gateway.", ex.getMessage());
        }
    }

    @Test
    @Order(23)
    public void ReportFindDisputesPaged_OrderBy_Id_With_Brand_VISA() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.CardBrand, "VISA")
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(24)
    public void ReportFindDisputesPaged_OrderBy_Id_With_Status_UnderReview() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStatus, DisputeStatus.UnderReview)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(25)
    public void ReportFindDisputesPaged_OrderBy_Id_With_Stage_Chargeback() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStage, DisputeStage.Chargeback)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(26)
    public void FindDocumentAssociatedWithDispute() throws ApiException {
        String disputeId = "DIS_SAND_abcd1235";
        String documentId = "DOC_MyEvidence_234234AVCDE-1";

        DisputeDocument response =
                ReportingService
                        .documentDisputeDetail(disputeId)
                        .where(SearchCriteria.DisputeDocumentId, documentId)
                        .execute();

        assertNotNull(response);
        assertEquals(documentId, response.getId());
        assertEquals("DOCUMENT_SINGLE", response.getType());
        assertNotNull(response.getBase64Content());
    }

    @Test
    @Order(27)
    public void FindDocumentAssociatedWithDispute_RandomDisputeId() throws ApiException {
        String disputeId = UUID.randomUUID().toString();
        String documentId = "DOC_MyEvidence_234234AVCDE-1";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .documentDisputeDetail(disputeId)
                    .where(SearchCriteria.DisputeDocumentId, documentId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40073", ex.getResponseText());
            assertEquals("Status Code: 400 - 101,Unable to locate dispute record for that ID. Please recheck the ID provided.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(28)
    public void FindDocumentAssociatedWithDispute_RandomDocumentId() throws ApiException {
        String disputeId = "DIS_SAND_abcd1235";
        String documentId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .documentDisputeDetail(disputeId)
                    .where(SearchCriteria.DisputeDocumentId, documentId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40071", ex.getResponseText());
            assertEquals("Status Code: 400 - 128,No document found, please recheck the values provided", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(29)
    public void FindDocumentAssociatedWithDispute_MissingDocId() throws ApiException {
        String disputeId = "DIS_SAND_abcd1235";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .documentDisputeDetail(disputeId)
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("disputeDocumentId cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    // ================================================================================
    // Settlement disputes
    // ================================================================================
    @Test
    @Order(30)
    public void ReportSettlementDisputeDetail() throws ApiException {
        String settlementDisputeId = "DIS_810";

        DisputeSummary response =
                ReportingService
                        .settlementDisputeDetail(settlementDisputeId)
                        .execute();

        assertNotNull(response);
        assertEquals(settlementDisputeId, response.getCaseId());
    }

    @Test
    @Order(31)
    public void ReportSettlementDisputeDetail_WrongID() throws ApiException {
        String disputeId = "DIS_666";

        boolean exceptionCaught = false;
        try {
            ReportingService
                    .settlementDisputeDetail(disputeId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40118", ex.getResponseText());
            assertEquals("Status Code: 404 - Disputes " + disputeId + " not found at this /ucp/settlement/disputes/DIS_666", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(32)
    public void ReportSettlementDisputeDetail_NullID() throws ApiException {
        boolean exceptionCaught = false;
        try {
            ReportingService
                    .settlementDisputeDetail(null)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields - account_id or account_name", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    //TODO - status = funded and "to_stage_time_created": "2020-12-17T23:59:59.999Z" added as default for filter
    @Test
    @Order(33)
    public void ReportFindSettlementDisputesPaged_OrderBy_Id() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(34)
    public void ReportFindSettlementDisputesPaged_OrderBy_ARN() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ARN, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(35)
    public void ReportFindSettlementDisputesPaged_OrderBy_Id_With_Status_UnderReview() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStatus, DisputeStatus.UnderReview)
                        .execute();

        assertNotNull(disputes);
    }

    @Test
    @Order(36)
    public void ReportFindSettlementDisputesPaged_OrderBy_Brand() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Brand, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    @Order(37)
    public void ReportFindSettlementDisputesPaged_OrderBy_Status() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Status, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Status, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    @Order(38)
    public void ReportFindSettlementDisputesPaged_OrderBy_Stage() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Stage, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    @Order(39)
    public void ReportFindSettlementDisputesPaged_OrderBy_FromStageTimeCreated() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromStageTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    @Order(40)
    public void ReportFindSettlementDisputesPaged_OrderBy_ToStageTimeCreated() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToStageTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    @Order(41)
    public void ReportFindSettlementDisputesPaged_OrderBy_AdjustmentFunding() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.AdjustmentFunding, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    @Order(42)
    public void ReportFindSettlementDisputesPaged_OrderBy_FromAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.FromAdjustmentTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    //TODO - lists not ordered correctly
    @Test
    @Order(43)
    public void ReportFindSettlementDisputesPaged_OrderBy_ToAdjustmentTimeCreated() throws ApiException {
        DisputeSummaryPaged disputesAsc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Ascending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesAsc);

        DisputeSummaryPaged disputesDesc =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.ToAdjustmentTimeCreated, SortDirection.Descending)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .execute();

        assertNotNull(disputesDesc);

        assertNotSame(disputesAsc, disputesDesc);
    }

    @Test
    @Order(44)
    public void ReportFindSettlementDisputesPaged_FilterBy_ARN() throws ApiException {
        String arn = "71400011129688701392096";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(arn, disputeSummary.getTransactionARN());
        }
    }

    @Test
    @Order(45)
    public void ReportFindSettlementDisputesPaged_FilterBy_ARN_NotFound() throws ApiException {
        String arn = "00000011129654301392121";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.AquirerReferenceNumber, arn)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(46)
    public void ReportFindSettlementDisputesPaged_FilterBy_Brand() throws ApiException {
        String[] cardBrands = {"VISA", "MASTERCARD", "AMEX", "DISCOVER"};

        for (String cardBrand : cardBrands) {

            DisputeSummaryPaged disputes =
                    ReportingService
                            .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                            .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                            .and(SearchCriteria.CardBrand, cardBrand)
                            .execute();

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes.getResults()) {
                String transactionCardType = disputeSummary.getTransactionCardType();
                if (transactionCardType != null) {
                    assertEquals(cardBrand, transactionCardType);
                }
            }
        }
    }

    //TODO - Brand = JCB results are returned even JCB is not in the agreed list
    @Test
    @Order(47)
    public void ReportFindSettlementDisputesPaged_FilterBy_Brand_NotFound() throws ApiException {
        String cardBrand = "Bank of America";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.CardBrand, cardBrand)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(48)
    public void ReportFindSettlementDisputesPaged_FilterBy_Status() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStatus, DisputeStatus.Funded)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(DisputeStatus.Funded.getValue(), disputeSummary.getCaseStatus());
        }
    }

    @Test
    @Order(49)
    public void ReportFindSettlementDisputesPaged_By_DepositDate() throws ApiException {
        DisputeSummaryPaged result =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .orderBy(DisputeSortProperty.Id, SortDirection.Descending)
                        .where(DataServiceCriteria.StartDepositDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndDepositDate, REPORTING_LAST_MONTH_DATE)
                        .execute();

        assertNotNull(result.getResults());
        for (DisputeSummary disputeSummary : result.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(disputeSummary.getDepositDate(), REPORTING_START_DATE));
            assertTrue(DateUtils.isBeforeOrEquals(disputeSummary.getDepositDate(), REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    @Order(50)
    public void ReportFindSettlementDisputesPaged_FilterBy_AllStages() throws ApiException {
        for (DisputeStage disputeStage : DisputeStage.values()) {
            DisputeSummaryPaged disputes =
                    ReportingService
                            .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                            .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                            .and(SearchCriteria.DisputeStage, disputeStage)
                            .execute();

            assertNotNull(disputes);
            for (DisputeSummary disputeSummary : disputes.getResults()) {
                assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
            }
        }
    }

    @Test
    @Order(51)
    public void ReportFindSettlementDisputesPaged_FilterBy_Stage() throws ApiException {
        DisputeStage disputeStage = DisputeStage.Chargeback;

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStage, disputeStage)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(disputeStage.getValue(), disputeSummary.getCaseStage());
        }

        DisputeStage disputeStageReversal = DisputeStage.Reversal;

        DisputeSummaryPaged disputesReversal =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(SearchCriteria.DisputeStage, disputeStageReversal)
                        .execute();

        assertNotNull(disputesReversal);
        for (DisputeSummary disputeSummary : disputesReversal.getResults()) {
            assertEquals(disputeStageReversal.getValue(), disputeSummary.getCaseStage());
        }

        assertNotSame(disputes, disputesReversal);
    }

    @Test
    @Order(52)
    public void ReportFindSettlementDisputesPaged_FilterBy_FromAndToStageTimeCreated() throws ApiException {
        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.EndStageDate, REPORTING_LAST_MONTH_DATE)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertTrue(DateUtils.isAfterOrEquals(disputeSummary.getCaseTime().toDate(), REPORTING_START_DATE));
            assertTrue(DateUtils.isBeforeOrEquals(disputeSummary.getCaseTime().toDate(), REPORTING_LAST_MONTH_DATE));
        }
    }

    @Test
    @Order(53)
    public void ReportFindSettlementDisputesPaged_FilterBy_SystemMidAndHierarchy() throws ApiException {
        String merchantId = "101023947262";
        String systemHierarchy = "055-70-024-011-019";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(merchantId, disputeSummary.getCaseMerchantId());
            assertEquals(systemHierarchy, disputeSummary.getMerchantHierarchy());
        }
    }

    @Test
    @Order(54)
    public void ReportFindSettlementDisputesPaged_FilterBy_WrongSystemMerchantId() throws ApiException {
        String merchantId = "000023947222";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(55)
    public void ReportFindSettlementDisputesPaged_FilterBy_WrongSystemHierarchy() throws ApiException {
        String systemHierarchy = "000-70-024-011-111";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.SystemHierarchy, systemHierarchy)
                        .execute();

        assertNotNull(disputes);
        assertEquals(0, disputes.getResults().size());
    }

    @Test
    @Order(56)
    public void ReportFindSettlementDisputesPaged_FilterBy_DepositId() throws ApiException {
        String DEPOSIT_ID = "DEP_2342423423";

        DisputeSummaryPaged disputes =
                ReportingService
                        .findSettlementDisputesPaged(FIRST_PAGE, PAGE_SIZE)
                        .where(DataServiceCriteria.StartStageDate, REPORTING_START_DATE)
                        .and(DataServiceCriteria.DepositReference, DEPOSIT_ID)
                        .execute();

        assertNotNull(disputes);
        for (DisputeSummary disputeSummary : disputes.getResults()) {
            assertEquals(DEPOSIT_ID, disputeSummary.getDepositReference());
        }
    }

    @Test
    @Order(57)
    public void ReportDisputeDetail_stageTime() throws ApiException {
        String disputeId = "DIS_SAND_abcd1234";
        String dateFormat = "dd-MM-yyyy";
        String expectedStageDate = DateUtils.toString(DateTime.now().minusDays(7).toDate(), dateFormat);

        DisputeSummary response =
                ReportingService
                        .disputeDetail(disputeId)
                        .execute();

        assertNotNull(response);
        assertEquals(disputeId, response.getCaseId());
        assertEquals(expectedStageDate, DateUtils.toString(response.getCaseStageTime().toDate(), dateFormat));
    }

    @Test
    @Order(58)
    public void ReportDisputeDetail_brandReference() throws ApiException {
        String disputeId = "DIS_SAND_abcd1234";

        DisputeSummary response =
                ReportingService
                        .disputeDetail(disputeId)
                        .execute();

        assertNotNull(response);
        assertEquals(disputeId, response.getCaseId());
        assertEquals("23423421342323A", response.getTransactionBrandReference());
    }
}
