package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.ProductCode;
import com.global.api.network.enums.ServiceLevel;
import com.global.api.network.enums.UnitOfMeasure;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class VapsPanTests {
    public VapsPanTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setHardwareLevel("3750");
        acceptorConfig.setSoftwareLevel("04010031");
        acceptorConfig.setOperatingSystemLevel("Q50016A6");

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_001_amex_x416() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        // Product Data
        ProductData productData = new ProductData(ServiceLevel.SelfServe);
        productData.add(
                ProductCode.Unleaded_Premium_Gas,
                UnitOfMeasure.Gallons,
                new BigDecimal("2.64"),
                new BigDecimal("1.429"),
                new BigDecimal("3.77")
        );

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_002_truncated_pan() throws ApiException {
        // create the track data object from full pan
        CreditTrackData track = TestCards.VisaSwipe();

        // get the truncated data
        String truncatedTrack = track.getTruncatedTrackData();
        assertNotNull(track.getTruncatedTrackData());

        // create new track data from truncated
        track.setValue(truncatedTrack);
        assertFalse(track.getTrackData().endsWith("null"));
    }

    @Test
    public void test_003_truncated_pan() throws ApiException {
        // create the track data object from full pan
        CreditTrackData track = new CreditTrackData();
        track.setValue("4111111111111111=2512");
    }
}
