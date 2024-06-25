package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
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
public class NWSFleetCorTests {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private NetworkGatewayConfig config;
    public NWSFleetCorTests() throws ApiException {
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

        ServicesContainer.configureService(config);

        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        fleetData = new FleetData();
        //fleetData.setServicePrompt("0");
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.PREMIUM_DIESEL_BLEND, UnitOfMeasure.Gallons, 1, 10);

        //FleetWide
        card = TestCards.FleetWideManual(true,true);
        track = TestCards.FleetWide();

        card = TestCards.FuelmanFleetManual(true,true);
        track = TestCards.FuelmanFleet();
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

    //Fuelman Fleet Authorization
    @Test
    public void test_001_fuelman_authorization() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

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
        fleetData.setServicePrompt("0");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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
    public void test_020_fuelman_reversal() throws ApiException {

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
                .withFleetData(fleetData)
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
        //assertEquals("002", sale.getResponseCode());

        System.out.println(sale.getAuthorizedAmount());

        Transaction response = sale.voidTransaction(new BigDecimal(8))
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .withFleetData(fleetData)
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



}
