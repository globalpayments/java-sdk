package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsFleetTests {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private AcceptorConfig acceptorConfig;
    NetworkGatewayConfig config;

    public VapsFleetTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

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
        acceptorConfig.setSupportWexAdditionalProducts(true);
//        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setVisaFleet2(false);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0003698521408");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

//        config.setMerchantType("5542");
//        ServicesContainer.configureService(config, "ICR");

        // MASTERCARD FLEET
        card = TestCards.MasterCardFleetManual(true, true);
        track = TestCards.MasterCardFleetSwipe();

        // VOYAGER FLEET
//        card = TestCards.VoyagerManual(true, true);
//        track = TestCards.VoyagerSwipe();

        fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

        // VISA
//        card = TestCards.VisaFleetManual(true, true);
//        track = TestCards.VisaFleetSwipe();
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_manual_authorization() throws ApiException {

        Transaction response = card.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal(1))
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
    public void test_001_manual_VisaFleet_authorization() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();

        Transaction response = card.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal(1))
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
    public void test_002_manual_sale_27_PDF0() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland,ProductDataFormat.HeartlandStandardFormat);
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

    @Test
    public void test_002_manual_sale_VisaFleet_PDF0() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();

        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Heartland,ProductDataFormat.HeartlandStandardFormat);
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

    @Test
    public void test_002_manual_sale_5_PCS2() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal("11.1234"), new BigDecimal("1.1789"), new BigDecimal("13.11"));

        Transaction response = card.charge(new BigDecimal(13.11))
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
    public void test_001_manual_authorization_completion_VisaFleet() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withFee(FeeType.TransactionFee,new BigDecimal(1))
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        Transaction response = preRresponse.preAuthCompletion(new BigDecimal(10))
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

        Transaction forced = NetworkService.forcedRefund(response.getTransactionToken())
                .execute();
    }

    @Test
    public void test_003_swipe_refund_by_transaction() throws ApiException {
        Transaction saleResponse = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("000", saleResponse.getResponseCode());

        Transaction refundResponse = saleResponse.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(refundResponse);

        HashMap<CardIssuerEntryTag, String> issuerData = refundResponse.getIssuerData();
        if(issuerData != null) {
            assertTrue(issuerData.containsKey(CardIssuerEntryTag.SwipeIndicator));
            assertNotEquals("0", issuerData.get(CardIssuerEntryTag.SwipeIndicator));
        }
        assertEquals("000", refundResponse.getResponseCode());
    }

    @Test
    public void test_003_swipe_refund_by_rebuilt_transaction() throws ApiException {
        Transaction saleResponse = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("000", saleResponse.getResponseCode());

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10"))
                .withNtsData(saleResponse.getNtsData())
                .withAuthorizationCode(saleResponse.getAuthorizationCode())
                .withPaymentMethod(track)
                .withSystemTraceAuditNumber(saleResponse.getSystemTraceAuditNumber())
                .withProcessingCode(saleResponse.getProcessingCode())
                .withAcquirerId(saleResponse.getAcquiringInstitutionId())
                .withTransactionTime(saleResponse.getOriginalTransactionTime())
                .build();

        Transaction refundResponse = rebuild.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(refundResponse);

        HashMap<CardIssuerEntryTag, String> issuerData = refundResponse.getIssuerData();
        if(issuerData != null) {
            assertTrue(issuerData.containsKey(CardIssuerEntryTag.SwipeIndicator));
            assertNotEquals("0", issuerData.get(CardIssuerEntryTag.SwipeIndicator));
        }
        assertEquals("000", refundResponse.getResponseCode());
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
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
        assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                .withReferenceNumber(sale.getReferenceNumber())
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

    @Test(expected = GatewayTimeoutException.class) @Ignore
    public void test_007_swipe_reverse_sale() throws ApiException {
        track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .execute();
    }

    @Test
    public void test_008_ICR_authorization() throws ApiException {
        card = TestCards.MasterCardFleetManual(true, true);
        track = TestCards.MasterCardFleetSwipe();
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

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

        // test_009
/*        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());*/
    }

    //void of partial approval
    @Test
    public void test_000_credit_VISAFleet_Void_partial_approval() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("40"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("40"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber(response.getReferenceNumber())
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

    @Test
    public void test_000_credit_MC_Void_partial_approval() throws ApiException {
        card = TestCards.MasterCardFleetManual(true, true);
        track = TestCards.MasterCardFleetSwipe();

        fleetData.setOdometerReading("000004");
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("005365");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("02", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal("40"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("40"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber(response.getReferenceNumber())
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
    
    @Test
    public void test_009_swipe_sale_product_01() throws ApiException {
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

    //Visa fleet with more than 6 products
    @Test
    public void test_010_swipe_sale_product_02_VisaFleet() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();
        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, new BigDecimal("1.99"), new BigDecimal("3.87"), new BigDecimal("478.99"));
        productData.add(ProductCode.UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0005"), new BigDecimal("0.33"),new BigDecimal("1.65"));
        productData.add(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Car_Wash, UnitOfMeasure.Units,  new BigDecimal("0001"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("1"), new BigDecimal("12.74"),new BigDecimal("12.74"));

        Transaction response = track.authorize(new BigDecimal(10),true)
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

    //5 NF and 0F Product
    @Test
    public void test_010_swipe_sale_product_02_VisaFleet_5NF() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();
        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific);
        productData.add(ProductCode.UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0005"), new BigDecimal("0.33"),new BigDecimal("1.65"));
        productData.add(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Car_Wash, UnitOfMeasure.Units,  new BigDecimal("0001"), new BigDecimal("0.66"),new BigDecimal("1.32"));

        Transaction response = track.authorize(new BigDecimal(10),true)
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

    @Test
    public void test_010_swipe_sale_product_02_VisaFleet_1F6NF() throws ApiException {
        card = TestCards.VisaFleetManual(true, true);
        track = TestCards.VisaFleetSwipe();
        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, new BigDecimal("1.99"), new BigDecimal("3.87"), new BigDecimal("478.99"));
        productData.add(ProductCode.UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0005"), new BigDecimal("0.33"),new BigDecimal("121.65"));
        productData.add(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("111.32"));
        productData.add(ProductCode.SUPER_UNLEADED_ETHANOL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.BIODIESEL, UnitOfMeasure.Units, new BigDecimal("0002"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Car_Wash, UnitOfMeasure.Units,  new BigDecimal("0001"), new BigDecimal("0.66"),new BigDecimal("1.32"));
        productData.add(ProductCode.Brake_Service, UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal("0.66"),new BigDecimal("1.32"));

        Transaction response = track.authorize(new BigDecimal(10),true)
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

    @Test
    public void test_011_swipe_sale_mc_product_03() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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

    //unleaded
    @Test
    public void test_012_swipe_sale_voyager_product_04() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
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
        productData.add(ProductCode.Regular_Leaded, UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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

    //Motor Oil
    @Test
    public void test_022_swipe_sale_mc_product_45() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("30", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("59", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("09", UnitOfMeasure.Quarts, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("27", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("23", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("14", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("33", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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

    //PreAuthorization
    @Test
    public void test_001_FuelMan_swipe_preAuthorizaiton_Completion() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add(ProductCode.Unleaded_Premium_Gas, UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction preRresponse = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

         productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
//        productData.add("01", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
//        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("03", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
//        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("02", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.preAuthCompletion(new BigDecimal(10))
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

    //auth completion
    @Test
    public void test_021_FuelMan_swipe_AuthCapture_cards() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

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
        assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("02", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));

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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

//    ----------------Voyager fleet EMV---------------------

    @Test
    public void test_027_Voyager_Fleet_Emv() throws ApiException {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("7088850950270000149=32010100010100600");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("9876");
        fleetData.setDriverId("1234");

        ProductData productData = new ProductData(ServiceLevel.FullServe,ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal("3.4664"), new BigDecimal("37.15"));

        Transaction response = rvalue.charge(new BigDecimal("37.15"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F0AA0000000049999C0001682023900840AA0000000049999C000168A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F6E1308 400003030001012059876123400987612340")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_027_Voyager_Fleet_Emv_9F6E() throws ApiException {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("7088850950270000149=32010100010100600");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("9876");
        fleetData.setDriverId("1234");

        ProductData productData = new ProductData(ServiceLevel.SelfServe,ProductCodeSet.IssuerSpecific);
        productData.add("09", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal("3.4664"), new BigDecimal("37.15"));

        Transaction response = rvalue.charge(new BigDecimal("37.15"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F0AA0000000049999C0001682023900840AA0000000049999C000168A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F6E1308400003030001012059876123400987612340")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_003_swipe_Voyager_refund_EMV() throws ApiException {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("7088850950270000149=32010100010100600");

        ProductData productData = new ProductData(ServiceLevel.FullServe,ProductCodeSet.IssuerSpecific);
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal("3.4664"), new BigDecimal("37.15"));

        Transaction response = rvalue.refund(new BigDecimal("31.75"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .withTagData("4F0AA0000000049999C0001682023900840AA0000000049999C000168A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F6E1308400003030001012059876123400987612340")
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

    @Test
    public void test_swipe_Voyager_Auth_Capture_EMV() throws ApiException {
        CreditTrackData rvalue = new CreditTrackData();
        rvalue.setValue("7088850950270000149=32010100010100600");

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = rvalue.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withTagData("4F0AA0000000049999C0001682023900840AA0000000049999C000168A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F6E1308400003030001012059876123400987612340")
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
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal("1"), new BigDecimal("30"), new BigDecimal("30"));

        NtsData ntsData = new NtsData();
        preRresponse.setNtsData(ntsData);
        Transaction response = preRresponse.capture(new BigDecimal("30"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F0AA0000000049999C0001682023900840AA0000000049999C000168A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F4104000000009F6E1308400003030001012059876123400987612340")
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
    public void test_proximity_entry_method_code_coverage() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        track.setEntryMethod(EntryMethod.Proximity);

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

    }
    @Test
    public void test_shipping_address_dataCollect_code_coverage_only() throws ApiException {

        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");
        address.setType(AddressType.Billing);

        acceptorConfig.setAddress(address);
        config.setAcceptorConfig(acceptorConfig);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        assertEquals("000",response.getResponseCode());
    }
    @Test
    public void test_currencyCode_code_coverage() throws ApiException {
        track.setEntryMethod(EntryMethod.Proximity);
        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("CAD")
                .withFee(FeeType.Surcharge,new BigDecimal(11))
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_dataCollect_resubmit() throws ApiException {

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

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
        assertEquals("000", preRresponse.getResponseCode());

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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction resubmitResp = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withForceToHost(true)
                .execute();

        assertNotNull(resubmitResp);
        assertEquals(resubmitResp.getResponseCode(),"000");
    }
}
