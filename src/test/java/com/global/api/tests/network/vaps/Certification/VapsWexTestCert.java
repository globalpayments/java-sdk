package com.global.api.tests.network.vaps.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsWexTestCert {
    private Address address;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;

    public VapsWexTestCert() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // Address details.
        address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0006182025511");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("4214");
        ServicesContainer.configureService(config);

//        ServicesContainer.configureService(config, "outside");
    }

    @Test
    public void test_060_Auth() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013048200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("48");
        fleetData.setEnteredData("1230");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_060_Auth_capture() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013048200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("48");
        fleetData.setEnteredData("1230");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("054", UnitOfMeasure.ImperialGallons, new BigDecimal(31), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_060_Auth_capture_retransmit() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19024013048200008");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("48");
        fleetData.setEnteredData("1230");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("154", UnitOfMeasure.Units, new BigDecimal(10), new BigDecimal(5), new BigDecimal(50));
        productData.addFuel("054", UnitOfMeasure.Gallons, new BigDecimal(50), new BigDecimal(10), new BigDecimal(500));
        productData.addFuel("179", UnitOfMeasure.Kilograms, new BigDecimal(11), new BigDecimal(11), new BigDecimal(121));
        productData.addFuel("104", UnitOfMeasure.Liters, new BigDecimal(05), new BigDecimal(5), new BigDecimal(25));
        productData.addFuel("021", UnitOfMeasure.OtherOrUnknown, new BigDecimal(10), new BigDecimal(15), new BigDecimal(150));

        Transaction response = card.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("62", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());


        Transaction resubmit = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_wex_sale() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("19");
        fleetData.setOdometerReading("85236");
        fleetData.setDriverId("0051");
        fleetData.setJobNumber("875236");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal(2), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel("400", UnitOfMeasure.Liters, new BigDecimal(4), new BigDecimal(10), new BigDecimal(10));
        productData.addFuel("950", UnitOfMeasure.Kilograms, new BigDecimal(5), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_wex_sale_Retransmit() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("19");
        fleetData.setOdometerReading("85236");
        fleetData.setDriverId("0051");
        fleetData.setJobNumber("875236");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal(2), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel("100", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.addNonFuel("400", UnitOfMeasure.Liters, new BigDecimal(4), new BigDecimal(10), new BigDecimal(10));
        productData.addFuel("950", UnitOfMeasure.Kilograms, new BigDecimal(5), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());


        Transaction resubmit = NetworkService.forcedSale(response.getTransactionToken())
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_wex_credit_Adjustment() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30512");
        fleetData.setServicePrompt("10");
        fleetData.setDriverId("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("200", UnitOfMeasure.Units, new BigDecimal("10"), new BigDecimal("52.73"));
        productData.addNonFuel("400", UnitOfMeasure.Gallons, new BigDecimal("5"), new BigDecimal("12"));
        productData.addNonFuel("121", UnitOfMeasure.OtherOrUnknown, new BigDecimal("20"), new BigDecimal("7"));
        productData.addNonFuel("103", UnitOfMeasure.OtherOrUnknown, new BigDecimal("30"), new BigDecimal("21.73"));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction refund = response.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTransactionMatchingData(new TransactionMatchingData("0000040067", "0630"))
                .execute();
        assertNotNull(refund);
        assertEquals(refund.getResponseMessage(), "000", refund.getResponseCode());
    }

    @Test
    public void test_wex_credit_Adjustment_retransmit() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("30512");
        fleetData.setServicePrompt("10");
        fleetData.setDriverId("123456");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("200", UnitOfMeasure.Units, new BigDecimal("10"), new BigDecimal("52.73"));

        Transaction response = card.charge(new BigDecimal(52.73))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction refund = response.refund(new BigDecimal(52.73))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTransactionMatchingData(new TransactionMatchingData("0000710001", "0630"))
                .execute();
        assertNotNull(refund);
        assertEquals(refund.getResponseMessage(), "000", refund.getResponseCode());

        Transaction forced = NetworkService.forcedRefund(refund.getTransactionToken())
                .execute();
        assertNotNull(forced);
        assertEquals("000", forced.getResponseCode());
    }
}

