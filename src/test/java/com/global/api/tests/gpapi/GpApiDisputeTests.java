package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.DisputeDocument;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.DisputeStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiDisputeTests {
    DisputeSummary dispute;

    public GpApiDisputeTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "GpApiConfig");
    }

    @Before
    public void Initialize() {
        dispute = new DisputeSummary();
        dispute.setCaseId("DIS_SAND_abcd1234");
    }

    @Test
    public void disputeAccept() throws ApiException {
        Transaction response =
                dispute
                        .accept()
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

    @Test
    public void disputeChallenge() throws ApiException {
        DisputeDocument newDocument = new DisputeDocument();
        newDocument.setType("SALES_RECEIPT");
        newDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(newDocument);

        Transaction response =
                dispute
                        .challenge(documents)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

    @Ignore
    // For invalid input Case IDs, the response is compressed but the hacking code related to the method:
    // Gateway.notCompressedResponseEndpoints( ... ) is avoiding to decompressed this endpoint
    // due to for valid input Case IDs the response is not compressed.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void disputeAccept_WrongId() throws ApiException {
        dispute.setCaseId("DIS_SAND_abcd1000");

        try {
            dispute
                    .accept()
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40067", ex.getResponseText());
            assertEquals("INVALID_DISPUTE_ACTION", ex.getResponseCode());
            assertEquals("Status Code: 400 - 124,Unable to accept for that id. Please check the Case id again.", ex.getMessage());
        }
    }

    @Ignore
    // For invalid input Case IDs, the response is compressed but the hacking code related to the method:
    // Gateway.notCompressedResponseEndpoints( ... ) is avoiding to decompressed this endpoint
    // due to for valid input Case IDs the response is not compressed.
    // TODO: Report error to GP-API team. Enable it when fixed.
    @Test
    public void disputeChallenge_WrongId() throws ApiException {
        dispute.setCaseId("DIS_SAND_abcd1000");

        DisputeDocument disputeDocument = new DisputeDocument();
        disputeDocument.setType("SALES_RECEIPT");
        disputeDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(disputeDocument);

        try {
            dispute
                    .challenge(documents)
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40060", ex.getResponseText());
            assertEquals("INVALID_DISPUTE_ACTION", ex.getResponseCode());
            assertEquals("Status Code: 400 - 117,Unable to challenge for that id. Please check the Case id again.", ex.getMessage());
        }
    }

    @Test
    public void disputeChallenge_MissingOptional_Type() throws ApiException {
        DisputeDocument newDocument = new DisputeDocument();
        newDocument.setBase64Content("R0lGODlhigPCAXAAACwAAAAAigPCAYf///8AQnv");

        ArrayList<DisputeDocument> documents = new ArrayList<>();
        documents.add(newDocument);

        Transaction response =
                dispute
                        .challenge(documents)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(DisputeStatus.Closed.getValue(), response.getResponseMessage());
    }

}