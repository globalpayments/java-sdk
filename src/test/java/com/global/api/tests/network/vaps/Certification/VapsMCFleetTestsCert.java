package com.global.api.tests.network.vaps.Certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsMCFleetTestsCert {
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private FleetData fleetData;
    private CreditTrackData track;
    private ProductData productData;

    public VapsMCFleetTestsCert() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

        // hardware software config values
        acceptorConfig.setHardwareLevel("S1");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setVisaFleet2(false);

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

        ServicesContainer.configureService(config);

        fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

        track = TestCards.MasterCardFleetSwipe();
    }

    @Test
    public void test_001_MCFleet_authorization() throws ApiException {
        track = TestCards.MasterCardFleetSwipe();

        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_001_MCFleet_auth_capture() throws ApiException {
        track = TestCards.MasterCardFleetSwipe();

        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel("01", UnitOfMeasure.Liters, new BigDecimal("1.01"), new BigDecimal(20), new BigDecimal(20));
        productData.addNonFuel("78", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_001_MCFleet_auth_capture_Retransmit() throws ApiException {
        track = TestCards.MasterCardFleetSwipe();

        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel("21", UnitOfMeasure.Liters, new BigDecimal(10.1), new BigDecimal(20), new BigDecimal(20));
        productData.addNonFuel("88", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));
        productData.addNonFuel("68", UnitOfMeasure.OtherOrUnknown, new BigDecimal(10), new BigDecimal(40), new BigDecimal(50));

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", capture.getResponseCode());

        Transaction resubmit = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_002_sale_MCFleet() throws ApiException {
        track = TestCards.MasterCardFleetSwipe();

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel(ProductCode.Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("11.12"), new BigDecimal("10.00"), new BigDecimal("111.2"));

        Transaction response = track.charge(new BigDecimal("111.2"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_002_sale_MCFleet_retransmit() throws ApiException {
        track = TestCards.MasterCardFleetSwipe();

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel(ProductCode.Ethanol_Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("31.12"), new BigDecimal("20.00"), new BigDecimal("311.2"));
        productData.addFuel(ProductCode.Regular_Leaded, UnitOfMeasure.ImperialGallons, new BigDecimal("11.08"), new BigDecimal("05.00"), new BigDecimal("181"));
        productData.addFuel(ProductCode.Ethanol_Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("27.05"), new BigDecimal("10.01"), new BigDecimal("211.01"));

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction resubmit = NetworkService.forcedSale(response.getTransactionToken())
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_003_swipe_sale_refund() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel(ProductCode.Ethanol_Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("31.12"), new BigDecimal("20.00"), new BigDecimal("11.2"));
        productData.addNonFuel(ProductCode.Regular_Leaded, UnitOfMeasure.ImperialGallons, new BigDecimal("11.08"), new BigDecimal("05.00"), new BigDecimal("81"));
        productData.addNonFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.ImperialGallons, new BigDecimal("11.08"), new BigDecimal("05.00"), new BigDecimal("55"));

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland, ProductDataFormat.HeartlandStandardFormat);
        productData.addFuel(ProductCode.Ethanol_Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("31.12"), new BigDecimal("10.00"), new BigDecimal("311.2"));
        productData.addFuel(ProductCode.Regular_Leaded, UnitOfMeasure.Liters, new BigDecimal("10.12"), new BigDecimal("20.00"), new BigDecimal("200.2"));

        Transaction refund = response.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(refund);

        // check message data
        pmi = refund.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200009", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", refund.getResponseCode());
    }

    @Test
    public void test_003_swipe_refund_retransmit() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction refund = response.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(refund);

        // check message data
        pmi = refund.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200009", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", refund.getResponseCode());

        Transaction forced = NetworkService.forcedRefund(refund.getTransactionToken())
                .execute();

        pmi = forced.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1221", pmi.getMessageTransactionIndicator());
        assertEquals("200009", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
    }
}
