package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.ReportingService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpEcomReportingTest extends BaseGpEComTest {

    public GpEcomReportingTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        ServicesContainer.configureService(config);
    }

    @Test
    @Order(1)
    public void GetTransactionDetail() throws ApiException {
        String orderId = "Bu6PN-F1Tt2VqMTP0Mvtwg";

        TransactionSummary response =
                ReportingService
                        .transactionDetail(orderId)
                        .execute();

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals("Xji7HzKcq79s7hmH", response.getSchemeReferenceData());
        assertEquals("17162823836225728", response.getTransactionId());
        assertEquals("U", response.getAvsResponseCode());
        assertEquals("M", response.getCvnResponseCode());
        assertEquals("00", response.getGatewayResponseCode());
        assertEquals("(00)[ test system ] Authorised", response.getGatewayResponseMessage());
        assertEquals("PASS", response.getFraudRuleInfo());
    }

    @Test
    @Order(2)
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
    @Order(3)
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
