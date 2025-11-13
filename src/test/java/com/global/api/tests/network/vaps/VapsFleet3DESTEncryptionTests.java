package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.enums.TrackNumber;
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
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsFleet3DESTEncryptionTests {

    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private AcceptorConfig acceptorConfig;

    public VapsFleet3DESTEncryptionTests() throws ApiException {
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
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportTerminalPurchaseRestriction(PurchaseRestrictionCapability.CHIPBASEDPRODUCTRESTRICTION);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setVisaFleet2(false);

        //DE 127
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        track.setCardType("MCFleet");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setTrackNumber(TrackNumber.TrackOne);
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track.setExpiry("2510");
        track.setFleetCard(true);

        // MC Fleet
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E00003"));
        card.setCardType("MCFleet");
        card.setExpYear(2024);
        card.setExpMonth(12);
        card.setFleetCard(true);

        fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.GlobalPayments);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

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
    public void test_002_manual_sale_27_PDF0() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments,ProductDataFormat.GlobalPaymentsStandardFormat);
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
    public void test_001_authorization() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
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
    public void test_003_swipe_sale() throws ApiException {
        Transaction saleResponse = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("000", saleResponse.getResponseCode());
    }

    @Test
    public void test_001_authorization_completion() throws ApiException {
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
                .withOfflineAuthCode("105683")
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
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
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
    public void test_Credit_auth_capture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_sale_reversal() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        response.setNtsData(new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Interchange_Authorized));

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse()
                .withReferenceNumber(response.getReferenceNumber())
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
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

    //void of partial approval
    @Test
    public void test_000_credit_Void_partial_approval() throws ApiException {
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
    public void test_000_credit_Void_partial_approval_auth() throws ApiException {

        fleetData.setOdometerReading("000004");
        fleetData.setDriverId("123456");
        fleetData.setVehicleNumber("005365");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("02", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.authorize(new BigDecimal("40"),true)
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
    public void test_001_refund_resubmit_DataCollectforce() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withOfflineAuthCode("105683")
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        NtsData ntsData = new NtsData();
        response.setNtsData(ntsData);
        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());

        Transaction resubmit = NetworkService.resubmitDataCollect(captureResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_014_forceRefund_10297() throws ApiException {
        Transaction response1 = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response1);

        Transaction response2 = response1.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response1.getSystemTraceAuditNumber()))
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response2);

        NtsData ntsData = new NtsData();
        response2.setNtsData(ntsData);
        Transaction response3 = response2.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response2.getSystemTraceAuditNumber()))
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response3);

        Transaction response = NetworkService.forcedRefund(response3.getTransactionToken())
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(response);

        assertEquals("000", response.getResponseCode());
    }

}
