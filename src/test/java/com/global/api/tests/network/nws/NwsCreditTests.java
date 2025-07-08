package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.elements.*;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsCreditTests {
    private CreditCardData card;
    private CreditTrackData track;
    private final NetworkGatewayConfig config;

    public NwsCreditTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        // Paypal data code values
//        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
//        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
//        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
//        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
//        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);


        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(false);
        acceptorConfig.setSupportsReturnBalance(false);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(false);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(false);
        acceptorConfig.setSupportsEmvPin(false);

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
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

        // with merchant type
//        config.setMerchantType("5542");
//        ServicesContainer.configureService(config, "ICR");

        // VISA
//         card = TestCards.VisaManual(true, true);
//         track = TestCards.VisaSwipe();

        // VISA CORPORATE
//        card = TestCards.VisaCorporateManual(true, true);
//        track = TestCards.VisaCorporateSwipe();

        // VISA PURCHASING
//        card = TestCards.VisaPurchasingManual(true, true);
//        track = TestCards.VisaPurchasingSwipe();

        // MASTERCARD
         card = TestCards.MasterCardManual(true, true);
         track = TestCards.MasterCardSwipe();
//
        // MASTERCARD PURCHASING
//        card = TestCards.MasterCardPurchasingManual(true, true);
//        track = TestCards.MasterCardPurchasingSwipe();

        // AMEX
//          card = TestCards.AmexManual(true, true);
//          track = TestCards.AmexSwipe();

        // DISCOVER
//         card = TestCards.DiscoverManual(false, true);
//          track = TestCards.DiscoverSwipe();
        
        // JCB
//        card = TestCards.JcbManual(true, true);
//        track = TestCards.JcbSwipe();

        //UnionPay
//        track = TestCards.UnionPaySwipe();
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    /*
    001 - Check Services
    002 - Check Services
    */

    @Test
    public void test_003_manual_authorization() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_004_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_006_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_026_balance_inquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_005_swipe_verify() throws ApiException {
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_008_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_authCapture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_007_swipe_sale_inside() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_009_swipe_stand_in_capture() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_swipe_voice_capture() throws ApiException {
        config.setMerchantType("5541"); //voice capture should be inside
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        Transaction sale = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }
    @Test
    public void test_006_swipe_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_016_ICR_authorization() throws ApiException {
        ServicesContainer.configureService(config,"ICR");
        Transaction response = track.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_017
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute("ICR");
        assertNotNull(captureResponse);
        
        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test //working
    public void test_028_reversal() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response;

        response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());


        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode(),
                response.getAcquiringInstitutionId()
        );
        Transaction reversal = transaction.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());

        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());
    }

    @Test
    public void test_012_swipe_partial_void_cancel() throws ApiException {
//        config.setMerchantType("5541");
//        ServicesContainer.configureService(config);

        Transaction sale = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test //working
    public void test_028_void_cancel() throws ApiException {
        Transaction response;

        response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());


        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode(),
                response.getAcquiringInstitutionId()
        );
        Transaction reversal = transaction.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());

        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("444", pmi.getFunctionCode());
        assertEquals("4358", pmi.getMessageReasonCode());
    }

    //void(Cancel of authorization)
    @Test
    public void test_008_authorization_void_cancel() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // partial approval cancellation
        Transaction cancel = response.voidTransaction()
                .execute();
        assertNotNull(cancel);

        pmi = cancel.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("444", pmi.getFunctionCode());
        assertEquals("4358", pmi.getMessageReasonCode());

        assertEquals(cancel.getResponseMessage(), "400", cancel.getResponseCode());
    }

    /** Visa CreditTestCases Starts */

    @Test
    public void test_006_authorization_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_026_balance_inquiry_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_005_swipe_verify_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_008_swipe_refund_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_authCapture_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_007_swipe_sale_inside_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_009_swipe_stand_in_capture_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_swipe_voice_capture_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        config.setMerchantType("5541"); //voice capture should be inside
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void_visa() throws ApiException {
        track = TestCards.VisaSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        Transaction sale = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    /** Amex CreditTestCases Starts */
    @Test
    public void test_006_authorization_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_026_balance_inquiry_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_005_swipe_verify_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_008_swipe_refund_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_authCapture_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_007_swipe_sale_inside_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_009_swipe_stand_in_capture_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_swipe_voice_capture_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        config.setMerchantType("5541"); //voice capture should be inside
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void_amex() throws ApiException {
        track = TestCards.AmexSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        Transaction sale = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    /** Visa Purchasing CreditTestCases Starts */
    @Test
    public void test_006_authorization_visa_purchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_026_balance_inquiry_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_005_swipe_verify_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_008_swipe_refund_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_authCapture_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_007_swipe_sale_inside_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_009_swipe_stand_in_capture_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_swipe_voice_capture_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        config.setMerchantType("5541"); //voice capture should be inside
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void_visaPurchasing() throws ApiException {
        track = TestCards.VisaPurchasingSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        Transaction sale = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    /** Discover CreditTestCases Starts */

    @Test
    public void test_006_authorization_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction response = track.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_026_balance_inquiry_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_005_swipe_verify_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction response = track.verify().execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("313000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_008_swipe_refund_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_authCapture_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_007_swipe_sale_inside_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_009_swipe_stand_in_capture_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Host_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_010_swipe_voice_capture_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        config.setMerchantType("5541"); //voice capture should be inside
        ServicesContainer.configureService(config);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                track
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_011_swipe_void_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        Transaction sale = track.charge(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_012_swipe_partial_void_discover() throws ApiException {
        track = TestCards.DiscoverSwipe();
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        Transaction sale = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    /**  Discover CreditTestCases Ends */

    @Test
    public void test_018_ICR_partial_authorization() throws ApiException {

        ServicesContainer.configureService(config,"ICR");
        Transaction response = track.authorize(new BigDecimal(40), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        
        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_020_ICR_auth_reversal() throws ApiException {
        ServicesContainer.configureService(config,"ICR");
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

//         check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_021_EMV_sale() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_022_EMV_authorization() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.authorize(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_023_EMV_02() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=1812201088280329");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820258008407A00000000410108E0A00000000000000000100950542400080009A031901199B02E8009C01005F24031812315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000003009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05FC50A000009F0E0500000000009F0F05F870A498009F10120210A5000F040000000000000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030622599F26080AB5BD8D4719AEEA9F2701809F330360F0C89F34030100029F3501219F360200059F3704DADCC7CB9F3901059F4005F000A0B0019F4104000000989F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction reversal = response.reverse().execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
    }

    @Test
    public void test_024_EMV_03() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089099130=221220114835949000");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("USD")
                .withTagData("4F07A000000004101050104D415354455243415244204445424954820218008407A00000000410108E120000000000000000420102055E0342031F00950580000080009A031901099B0268009C01405F24032212315F25031711015F2A0208405F300202015F3401119F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00003220000000000000000000000FF9F12104D6173746572636172642044656269749F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030647199F26084233C50A9D5D7FA29F2701809F330360F0C89F34035E03009F3501219F360201259F3704FF4CA1CD9F3901059F4005F000A0B0019F4104000000809F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(voidResponse.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_025_EMV_04() throws ApiException {
        ServicesContainer.configureService(config,"ICR");
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089010434=22122019882803290000");

        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D61737465724361726457135413330089010434D22122019882803290000F5A085413330089010434820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F201A546573742F4361726420313020202020202020202020202020205F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901059F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "002", response.getResponseCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test
    public void test_152_sale_with_cashAdvanced() throws ApiException {
        Transaction response = track.cashAdvanced(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("013000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_021_swipe_AuthCancel_cards() throws ApiException {

        Transaction preRresponse = track.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        Transaction response = preRresponse.cancel()
                .withReferenceNumber(preRresponse.getReferenceNumber())
                .execute();
        assertNotNull(response);

        pmi = response.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());

    }

    @Test
    public void test_0169_authCapture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }


    @Test
    public void test_sale_reversal() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


    /** Paypal TestCases starts */
    @Test
    public void test_006_paypal_outside_authorization() throws ApiException {
        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction response = paypaltrack.authorize(new BigDecimal(10), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //AuthCapture - working
    @Test
    public void test_016_paypal_outside_authCapture() throws ApiException {
        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction response = paypaltrack.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }


    //void cancel of authorization
    @Test //working
    public void test_028_paypal_outside_void_cancel() throws ApiException {
        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction response = paypaltrack.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());


        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
                paypaltrack,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode(),
                response.getAcquiringInstitutionId()
        );
        Transaction reversal = transaction.voidTransaction(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());

        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("444", pmi.getFunctionCode());
        assertEquals("4358", pmi.getMessageReasonCode());
    }

    @Test
    public void test_012_paypal_authorization_partial_void_cancel() throws ApiException {
        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction sale = paypaltrack.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        System.out.println(sale.getAuthorizedAmount());
        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    // paypal sale void partial
    @Test
    public void test_012_paypal_outside_partial_void() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction sale = paypaltrack.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(sale);
        assertEquals("002", sale.getResponseCode());

        System.out.println(sale.getAuthorizedAmount());

        Transaction response = sale.voidTransaction(sale.getAuthorizedAmount())
                .withPartialApproval(true)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    //PayPal inside return
    @Test
    public void test_008_paypal_inside_swipe_refund() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction response = paypaltrack.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200030", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //PayPal inside sale reversal
    @Test
    public void test_Paypal_inside_sale_reversal() throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        CreditTrackData paypaltrack = new CreditTrackData();
        paypaltrack.setValue("%B6506001000010029^TEST CARD/DISCOVER        ^2512101051012345678901999123123?;6506001000010029=25121010051012345678?");

        Transaction response = paypaltrack.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
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
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }
    /**  paypal testcases ends */

    @Test
    public void test_014_swipe_reverse_sale_Codecoverage() throws ApiException {
        config.setProtocolType(ProtocolType.Async);
        config.setMessageType(MessageType.NoMessage);
        ServicesContainer.configureService(config);

        card.charge(0).withCurrency("INR").execute();
    }

    @Test
    public void test_014_swipe_authorize_Codecoverage() {

        GatewayTimeoutException formatException = assertThrows(GatewayTimeoutException.class,
                () -> card.authorize(new BigDecimal(0))
            .withCurrency("USD")
            .withModifier(TransactionModifier.Offline)
                .withOfflineAuthCode("190")
            .execute());

        assertEquals("The gateway did not respond within the given timeout.", formatException.getMessage());
    }

    @Test
    public void test_014_authCapture_Codecoverage_GatewayTimoutExp() throws ApiException {
        card.setCardHolderName("ABC XYZ");
        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.reverse(new BigDecimal(12))
                .withCurrency("USD")
                .withHostResponseCode("911")
                .execute();
        assertNotNull(captureResponse);
    }

    @Test
    public void test_014_authCapture_Codecoverage() throws ApiException {
        card.setCardHolderName("ABC XYZ");
        Transaction response = card.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_024_EMV_Codecoverage() throws ApiException {
        //only for code coverage
        track.setEntryMethod(EntryMethod.Proximity);
        CreditTrackData track = new CreditTrackData();
        track.setValue("5413330089099130=221220114835949000");

        Transaction response = track.charge(new BigDecimal(6))
                .withCurrency("INR")
                .withTagData("4F07A000000004101050104D415354455243415244204445424954820218008407A00000000410108E120000000000000000420102055E0342031F00950580000080009A031901099B0268009C01405F24032212315F25031711015F2A0208405F300202015F3401119F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FFC09F090200029F0D05B0509C88009F0E0500000000009F0F05B0709C98009F10120110A00003220000000000000000000000FF9F12104D6173746572636172642044656269749F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030647199F26084233C50A9D5D7FA29F2701809F330360F0C89F34035E03009F3501219F360201259F3704FF4CA1CD9F3901059F4005F000A0B0019F4104000000809F4E0D54657374204D65726368616E74")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(voidResponse.getResponseMessage(), "400", voidResponse.getResponseCode());
    }
    @Test
    public void test_006_authorization_with_fee_type_22() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge,new BigDecimal(5))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

    }
    @Test
    public void test_exception_handling_code_coverage() throws ApiException {
        NtsData ntsData = new NtsData();
        card = TestCards.VisaManual(true,true);

        Transaction sale = card.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        sale.setNtsData(ntsData);
    }
    @Test
    public void test_contactLessMsd_entry_method_emv_for_code_coverage() throws ApiException {
        track = TestCards.VisaSwipe();
        track.setEntryMethod(EntryMethod.Proximity);

        Transaction sale = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withTagData("4F07A0000000041010500A4D617374657243617264820238008407A00000000410108E0A00000000000000001F00950500008080009A031901099B02E8009C01405F24032212315F25030401015F2A0208405F300202015F3401009F01060000000000019F02060000000006009F03060000000000009F0607A00000000410109F0702FF009F090200029F0D05B8508000009F0E0500000000009F0F05B8708098009F10120110A0800F22000065C800000000000000FF9F120A4D6173746572436172649F160F3132333435363738393031323334359F1A0208409F1C0831313232333334349F1E0831323334353637389F21030710109F26080631450565A30B759F2701809F330360F0C89F34033F00019F3501219F360200049F3704C6B1A04F9F3901919F4005F000A0B0019F4104000000869F4C0865C862608A23945A9F4E0D54657374204D65726368616E74")
                .execute();

        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
    }

    @Test
    public void test_gateway_timeout_exception_code_coverage() throws ApiException {
        String token = "ASU5MDQ1ICAgIDA0MCBXIDk5OTAxMlAwMjg4MDAwOTMyOTIzOTkyMTAxMDMxMjA2NDYwMDE5OTkwMTcwMjAwNTQ3MzUwMDAwMDAwMDAxNCAgIDEyMjUxMDU2ODMgMDAwMTAwMDAyMDAwMTAzMTIwNjQ2MTIwODFFMTcwMDEwMTAxMDMyNDEyMDY0NjEwMDAwMDAwMDAwMDAwMDA5OTAwMDAwMDAwMzQ4MDAwMDAwNjQxMTEyMDE3NDAwMDAxMDAwMDAwMDAxNzQxMDIwMTc0MDAwMDEwMDAwMDAwMDE3NDQ2MDAxNzQwMDAwMTAwMDAwMDAwMTc0NDAwMDAwMDAwMDAzMDAwMDAwMDE0MjIwMDAzMjMzMDAwMDAwMDAwMDAwMDAwMDA=";
        Transaction response = NetworkService.resubmitDataCollect(token)
                .execute();

        assertNotNull(response);
        assertEquals("000",response.getResponseCode());
    }
    @Test
    public void test_currency_AED_code_coverage() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("AED")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

    }
    @Test
    public void test_013_visa_encrypted_follow_on() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                track,
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            Transaction reversal = recreated.reverse(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
            assertNotNull(reversal);

        });
        assertEquals("The original processing code should be specified when performing a reversal.", builderException.getMessage());
    }
    @Test
    public void DE48_35_Name_tests_code_coverage() {
        String original = "00John Q Public";

        DE48_Name element = new DE48_Name().fromByteArray(original.getBytes());
        assertEquals(DE48_NameType.CardHolderName, element.getNameType());
        assertEquals(DE48_NameFormat.FreeFormat, element.getNameFormat());
        assertEquals("John Q Public", element.getName());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));

        original = "01Jane\\\\Doe";

        element = new DE48_Name().fromByteArray(original.getBytes());
        assertEquals(DE48_NameType.CardHolderName, element.getNameType());
        assertEquals(DE48_NameFormat.Delimited_FirstMiddleLast, element.getNameFormat());
        assertEquals("Jane", element.getFirstName());
        assertEquals("", element.getMiddleName());
        assertEquals("Doe", element.getLastName());

        buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }
    @Test
    public void test_unsupported_transaction_code_coverage_only() {

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            boolean tokenExpiry = track.updateTokenExpiry();
            assertTrue(tokenExpiry);
        });
        assertEquals("Token cannot be null", builderException.getMessage());
    }
    @Test
    public void test_unsupported_transaction_code_coverage_only_tokenize() {

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            String tokenize = track.tokenize();
            assertNotNull(tokenize);

        });
        assertEquals("Token cannot be null", builderException.getMessage());
    }
    @Test
    public void test_unsupported_transaction_code_coverage_only_delete_token() {

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            boolean tokenExpiry = track.deleteToken();
            assertTrue(tokenExpiry);

        });
        assertEquals("Token cannot be null", builderException.getMessage());
    }

    @Test
    public void test_002_truncated_pan_code_coverage() {
        // create the track data object from full pan
        CreditTrackData track = TestCards.VisaSwipe();

        // get the truncated data
        String truncatedTrack = track.getTruncatedTrackData();
        assertNotNull(track.getTruncatedTrackData());

        // create new track data from truncated
        track.setValue(truncatedTrack);
        assertFalse(track.getTrackData().endsWith("null"));
    }
    /** forwarding data code coverage test cases */
    @Test
    public void ForwardingData_tests_code_coverage() {
        String original = "013DE3363D001G112       F000019990E00003                29086EC77231A1435ECAF4E5EA9BEC8CC8446DC92D5A99DDD9F1CE1BAC79D7115C1224B380C05DCBBA2FD0D7F18074392EC8D800863BD43EC0FDA7E43FA1C303C6C540F6297C76A4                                                                                                                                                ";

        DE127_ForwardingData element = new DE127_ForwardingData().fromByteArray(original.getBytes());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }
    @Test
    public void ForwardingData_tests_code_coverage_tokenization() {
        String original =     "01TOK216TD001G222       650000011573667                 1DD5C11868C9717809EDD0BB12ACF61C                                                                                                2510                                    ";

        DE127_ForwardingData element = new DE127_ForwardingData().fromByteArray(original.getBytes());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }


}
