package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsCvnTests {
    public VapsCvnTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.OnCardSecurityCode);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.7eleven.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_amex_match() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, false);
        card.setCvn("0101");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_amex_mismatch() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, false);
        card.setCvn("0102");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_003_discover_match() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, false);
        card.setCvn("103");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_004_discover_mismatch() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, false);
        card.setCvn("104");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_005_discover_not_processed() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, false);
        card.setCvn("105");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_006_discover_cid_on_card() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, false);
        card.setCvn("106");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_007_mastercard_match() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, false);
        card.setCvn("107");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_mastercard_mismatch() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, false);
        card.setCvn("108");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_009_mastercard_not_processed() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, false);
        card.setCvn("109");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_010_visa_match() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, false);
        card.setCvn("110");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_visa_mismatch() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, false);
        card.setCvn("111");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_012_visa_not_processed() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, false);
        card.setCvn("112");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_013_visa_cid_on_card() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, false);
        card.setCvn("113");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("107", response.getResponseCode());
    }

    @Test
    public void test_014_mastercard_fleet_match() throws ApiException {
        CreditCardData card = TestCards.MasterCardFleetManual(false, false);
        card.setCvn("107");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_015_visa_fleet_match() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, false);
        card.setCvn("110");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    //Input Mode 6 - Key Entry (Manual Entry)
    //001. Run a American Express 1100 Authorization

    //002. Run a Discover 1100 Authorization
    //003. Run a MasterCard 1100 Authorization
    //007. Run a American Express MTI 1200 Sale
    //008. Run a Discover MTI 1200 Sale
    //009. Run a MasterCard MTI 1200 Sale
    //010. Run a Visa MTI 1200 Sale
    //011. Run a MasterCard Fleet MTI 1200 Sale
    //012. Run a Visa Fleet MTI 1200 Sale
    //Input Mode 2 – Magnetic Stripe Read DE 45 Track 1 or DE 35 Track 2 Data (Inside)
    //022. Run a Visa MTI 1200 Sale
}
