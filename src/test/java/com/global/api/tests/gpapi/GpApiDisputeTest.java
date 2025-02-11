package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.DisputeStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class GpApiDisputeTest extends BaseGpApiTest {
    DisputeSummary dispute;

    public GpApiDisputeTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @BeforeEach
    public void Initialize() {
        dispute = new DisputeSummary();
        dispute.setCaseId("DIS_SAND_abcd1234");
    }

    @Test
    public void DisputeAccept() throws ApiException {
        Transaction response =
                dispute
                        .accept()
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

    @Test
    public void DisputeAccept_WrongId() throws ApiException {
        dispute.setCaseId("DIS_SAND_bbbb1111");

        boolean exceptionCaught = false;
        try {
            dispute
                    .accept()
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40067", ex.getResponseText());
            assertEquals("INVALID_DISPUTE_ACTION", ex.getResponseCode());
            assertEquals("Status Code: 400 - 124,Unable to accept for that id. Please check the Case id again.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void DisputeChallenge() throws ApiException {
        DisputeDocument newDocument = new DisputeDocument();
        newDocument.setType("SALES_RECEIPT");
        newDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(newDocument);

        Transaction response =
                dispute
                        .challenge(documents)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

    @Test
    public void DisputeChallenge_MultipleDocuments() throws ApiException {
        dispute.setCaseId("DIS_SAND_abcd1241");

        DisputeDocument firstDocument = new DisputeDocument();
        firstDocument.setType("SALES_RECEIPT");
        firstDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        DisputeDocument secondDocument = new DisputeDocument();
        secondDocument.setType("TERMS_AND_CONDITIONS");
        secondDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(firstDocument);
        documents.add(secondDocument);

        Transaction response =
                dispute
                        .challenge(documents)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(DisputeStatus.UnderReview.getValue(), response.getResponseMessage());
    }

    @Test
    public void DisputeChallenge_MultipleDocuments_ClosedStatus() throws ApiException {
        DisputeDocument firstDocument = new DisputeDocument();
        firstDocument.setType("SALES_RECEIPT");
        firstDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        DisputeDocument secondDocument = new DisputeDocument();
        secondDocument.setType("TERMS_AND_CONDITIONS");
        secondDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(firstDocument);
        documents.add(secondDocument);

        boolean exceptionCaught = false;
        try {
            dispute
                    .challenge(documents)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40072", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - 131,The dispute stage, Retrieval, can be challenged with a single document only. Please correct the request and resubmit", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void DisputeChallenge_MissingDocument() throws ApiException {
        boolean exceptionCaught = false;

        try {
            dispute
                    .challenge(new ArrayList<>())
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40065", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("Status Code: 400 - Unable to challenge as No document provided with the request", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void DisputeChallenge_MissingOptional_Type() throws ApiException {
        DisputeDocument newDocument = new DisputeDocument();
        newDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(newDocument);

        Transaction response =
                dispute
                        .challenge(documents)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

    @Test
    public void DisputeChallenge_WrongId() throws ApiException {
        dispute.setCaseId("DIS_SAND_abcd1000");

        DisputeDocument disputeDocument = new DisputeDocument();
        disputeDocument.setType("SALES_RECEIPT");
        disputeDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(disputeDocument);

        boolean exceptionCaught = false;
        try {
            dispute
                    .challenge(documents)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40060", ex.getResponseText());
            assertEquals("INVALID_DISPUTE_ACTION", ex.getResponseCode());
            assertEquals("Status Code: 400 - 117,Unable to challenge for that id. Please check the Case id again.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

}
