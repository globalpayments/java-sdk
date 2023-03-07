package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
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
public class GpEcomReportingTest {
    public GpEcomReportingTest() throws ApiException {
        GpEcomConfig config = new GpEcomConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("api");
        config.setSharedSecret("secret");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void GetTransactionDetail() throws ApiException {
        String orderId = "rMuOHxP5SCyCzOYE8mKCsA";

        TransactionSummary response =
                ReportingService
                        .transactionDetail(orderId)
                        .execute();

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals("5CoDxmuV5efGltP9", response.getSchemeReferenceData());
        assertEquals("16776985695761244", response.getTransactionId());
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

}