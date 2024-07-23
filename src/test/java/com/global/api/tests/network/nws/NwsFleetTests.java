package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NetworkGatewayType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsFleetTests {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private NetworkGatewayConfig config;
    public NwsFleetTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
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
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        // MASTERCARD FLEET
        card = TestCards.MasterCardFleetManual(true, true);
        track = TestCards.MasterCardFleetSwipe();

        // VOYAGER FLEET
//        card = TestCards.VoyagerManual(true, true);
//        track = TestCards.VoyagerSwipe();

        fleetData = new FleetData();
        //fleetData.setServicePrompt("0");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

        // VISA
//        card = TestCards.VisaFleetManual(true, true);
//        track = TestCards.VisaFleetSwipe();

        //FleetWide
//        card = TestCards.FleetWideManual(true,true);
//        track = TestCards.FleetWide();

//        card = TestCards.FuelmanFleetManual(true,true);
//        track = TestCards.FuelmanFleet();
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_manual_authorization() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_002_manual_sale() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_001_swipe_authorization() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("00");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_003_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200009", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //auth completion
    @Test
    public void test_021_swipe_AuthCapture_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_004_swipe_stand_in_capture() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_swipe_voice_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_006_swipe_void() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
        //assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
              //  .withReferenceNumber(sale.getReferenceNumber())
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    //Reversal
    @Test
    public void test_008_authorization_reversal() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1),true)
                .withCurrency("USD")
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
        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        // partial approval cancellation
        Transaction reversal = response.cancel()
                .withReferenceNumber(response.getReferenceNumber())
                .execute();
        assertNotNull(reversal);

        pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());

    }

    @Test
    public void test_009_swipe_sale_product_01() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_010_swipe_sale_product_02() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("02", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_011_swipe_sale_mc_product_03() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("03", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_012_swipe_sale_voyager_product_04() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("04", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_013_swipe_sale_voyager_product_09() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("09", UnitOfMeasure.Quarts, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_014_swipe_sale_voyager_product_14() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("14", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_015_swipe_sale_mc_product_16() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("16", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_016_swipe_sale_voyager_product_23() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("23", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_017_swipe_sale_voyager_product_27() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("27", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_018_swipe_sale_mc_product_30() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("30", UnitOfMeasure.Quarts, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_019_swipe_sale_voyager_product_33() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("33", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_020_swipe_sale_product_39() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("39", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_021_swipe_sale_mc_product_41() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("41", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_022_swipe_sale_mc_product_45() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("45", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_023_swipe_sale_voyager_product_59() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("59", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        //productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_024_swipe_sale_mc_product_79() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("79", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_025_swipe_sale_mc_product_99() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("99", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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
    public void test_026_swipe_sale_mc_product_all() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("059", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("009", UnitOfMeasure.Quarts, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("027", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("023", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("014", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("033", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(70))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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

    //void of partial approval MC Fleet
    @Test
    public void test_000_MC_Void_partial_approval() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("111"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("111"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(50))
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    //void of partial approval visa Fleet
    @Test
    public void test_000_Visa_Void_partial_approval() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("32"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        //assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("111"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(12))
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }


    @Test
    public void test_021_swipe_AuthCancel_cards() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("005", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("045", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.cancel()
          .withReferenceNumber("0000")
          .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());

    }


    @Test
    public void test_006_swipe_void_cancel() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
       // assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());



//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response1 = response.cancel()
                .withReferenceNumber("0000")
                .execute();

        // check response
        assertEquals("400", response1.getResponseCode());
    }

    @Test
    public void test_000_swipe_void_partial_approval() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("100"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("50"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }


    //void(Cancel of authorization)
    @Test
    public void test_008_authorization_void_cancel() throws ApiException {

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.ClientSpecificAddendum_2);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("05", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.Kilograms, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("05", UnitOfMeasure.Pounds, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.authorize(new BigDecimal(1),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        // partial approval cancellation
        Transaction reversal = response.cancel()
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(reversal);

        pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
    }

    //Revesal
    @Test
    public void test_020_ICR_auth_reversal() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.ClientSpecificAddendum_2);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
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
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


    //Wex Authorize
    @Test
    public void test_001_WEX_Authorization_1100() throws ApiException {
        config.setTerminalId("GRMKCONA02");
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
//        card.setValue("6900460420006149231=19020013000200000");
        card.setValue("6900460430001234566=12241004658100000");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.ClientSpecificAddendum_2);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));

//        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
//        productData.add("074", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));


        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withProductData(productData)
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


    //Fleetwide Reversal
    @Test
    public void test_020_sale_reversal() throws ApiException {

//        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland);
//        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("02", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


    //Fuelman Fleet Authorization
    @Test
    public void test_001_fuelman_authorization() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    // FuelMan Fleet AuthCapture
    @Test
    public void test_021_Fuelman_AuthCapture_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction preRresponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                //.withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Fuelman Fleet VoiceCapture
    @Test
    public void test_005_fuelman_voice_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Fuelman Fleet Charge
    @Test
    public void test_009_fuelman_sale_product_01() throws ApiException {
//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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

    //Fuelman Fleet Reversal
    @Test
    public void test_020_fuel_reversal() throws ApiException {

//        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland);
//        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("02", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    //Fuelman Fleet StandInCapture
    @Test
    public void test_004_fuelman_stand_in_capture() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Fuelman Fleet void
    @Test
    public void test_011_swipe_void() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    //Fuelman Fleet cancelation of authorization
    @Test
    public void test_021_fuelman_AuthCancel_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
//        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        Transaction response = preRresponse.cancel()
                .withReferenceNumber("0000")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());

    }

    //Fuelman Partial void
    @Test
    public void test_012_fuelman_partial_void() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction sale = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        System.out.println(sale.getAuthorizedAmount());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }


    //Voyager Authorize
    @Test
    public void test_021_voyger_swipe_Authorize_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");

        Transaction preRresponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

    }

    //Voyager Auth Capture
    @Test
    public void test_021_swipe_AuthCapture_voyager_card() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction preRresponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
       // assertEquals("000", preRresponse.getResponseCode());
        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("3");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }


    //Voyager Voice Capture
    @Test
    public void test_005_voyager_swipe_voice_capture() throws ApiException {
//        fleetData.setOdometerReading("987654");
//        fleetData.setDriverId("001001");
//        fleetData.setServicePrompt("3");

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track,
                "1220",
                "0000",
                "0000"
        );

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Voyager Sale
    @Test
    public void test_009_voyager_swipe_sale_product_01() throws ApiException {
        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("3");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
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

    //Voyager Void (Cancellation of Authorize)
    @Test
    public void test_021_swipe_voyager_AuthCancel_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("3");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        //assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("005", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("045", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.cancel()
                .withReferenceNumber("0000")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);

        pmi = response.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

       // assertEquals(response.getResponseMessage(), "400", response.getResponseCode());

    }

    //Voyager Void
    @Test
    public void test_006_voyage_swipe_void() throws ApiException {
//        fleetData.setOdometerReading("987654");
//        fleetData.setDriverId("001001");
//        fleetData.setServicePrompt("0");

//        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
//        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
        //assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                //  .withReferenceNumber(sale.getReferenceNumber())
                .withReferenceNumber("0000")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }


    //Voyager void of partial approval
    @Test
    public void test_000_voyager_void_partial_approval() throws ApiException {
        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("3");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("40"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        //assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        //assertNotEquals(new BigDecimal("20"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(new BigDecimal("20"))
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    //Voyager Reversal
    @Test
    public void test_sale_voyager_reversal() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        fleetData.setOdometerReading("987654");
        fleetData.setDriverId("001001");
        fleetData.setServicePrompt("3");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));


        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_002_manual_sale_27_PDF0_code_coverage_only() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland,ProductDataFormat.ANSI_X9_TG23_Format);
        productData.add(ProductCode.Regular_Leaded, UnitOfMeasure.Gallons, new BigDecimal("11.12"), new BigDecimal("10.00"), new BigDecimal("111.2"));

        Transaction response = card.charge(new BigDecimal("111.2"))
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

}
