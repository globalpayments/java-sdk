package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.ReportingService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpEcomReportingTest extends BaseGpEComTest {

    public GpEcomReportingTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        ServicesContainer.configureService(config);
    }

    @Test
    public void GetTransactionDetail() throws ApiException {
        String orderId = "M59u9iEDRyCHPbaj09scsg";

        TransactionSummary response =
                ReportingService
                        .transactionDetail(orderId)
                        .execute();

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals("wliynvnlLYmjo0K6", response.getSchemeReferenceData());
        assertEquals("16981502011346656", response.getTransactionId());
        assertEquals("U", response.getAvsResponseCode());
        assertEquals("M", response.getCvnResponseCode());
        assertEquals("00", response.getGatewayResponseCode());
        assertEquals("(00)[ test system ] Authorised", response.getGatewayResponseMessage());
        assertEquals("PASS", response.getFraudRuleInfo());
    }

    @Test
    public void GetTransactionDetail_WithRandomId() throws ApiException {
        String orderId = UUID.randomUUID().toString();

        try {
            ReportingService
                    .transactionDetail(orderId)
                    .execute();
        } catch (GatewayException ex) {
            assertEquals("508", ex.getResponseCode());
            assertEquals("Original transaction not found.", ex.getResponseText());
        }
    }

    @Test
    public void GetTransactionDetail_WithNullId() throws ApiException {
        try {
            ReportingService
                    .transactionDetail(null)
                    .execute();
        } catch (BuilderException ex) {
            assertEquals("transactionId cannot be null for this transaction type.", ex.getMessage());
        }
    }

}