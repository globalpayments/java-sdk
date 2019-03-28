package com.global.api.tests.realex;

import com.global.api.ServicesContainer;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.ReportingService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RealexReportingTests {
    public RealexReportingTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("heartlandgpsandbox");
        config.setAccountId("api");
        config.setSharedSecret("secret");
        config.setRefundPassword("refund");
        config.setServiceUrl("https://api.sandbox.realexpayments.com/epage-remote.cgi");

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_001_TransactionDetail() throws ApiException {
        TransactionSummary response = ReportingService.transactionDetail("vfJaH8liSF-3f394tbuPYA")
                .execute();
        assertNotNull(response);
        assertEquals("vfJaH8liSF-3f394tbuPYA", response.getOrderId());
    }
}
