package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE123_ReconciliationTotals_nws;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
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
import java.util.LinkedList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsBatchTests {
    private String configName = "default";
    private BatchProvider batchProvider;
    private  NetworkGatewayConfig config;


    public NwsBatchTests() throws ApiException {
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

        // hardware software config values
        acceptorConfig.setHardwareLevel("62");
        acceptorConfig.setSoftwareLevel("858.5.08");
        acceptorConfig.setOperatingSystemLevel("00");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        //DE48-34 message configuration values
        acceptorConfig.setPerformDateCheck(true);
        acceptorConfig.setEchoSettlementData(true);
        acceptorConfig.setIncludeLoyaltyData(false);

        batchProvider = BatchProvider.getInstance();

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setSecondaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setPrimaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setMerchantType("5541");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(batchProvider);

        ServicesContainer.configureService(config);

        ServicesContainer.configureService(config, "NoBatch");
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_resubmits_provider() throws ApiException {
        configName = "default";

        creditSale(10);
        creditAuth(20);
        debitSale(30);
        debitAuth(50);

        Transaction response = BatchService.closeBatch(1, new BigDecimal(30), new BigDecimal(80))
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
    }

    @Test
    public void test_002_resubmits_no_provider() throws ApiException {
        configName = "NoBatch";

        Transaction creditSale = creditSale(10);
        assertNotNull(creditSale.getTransactionToken());

        Transaction creditAuth = creditAuth(20);
        assertNotNull(creditAuth.getTransactionToken());

        Transaction debitSale = debitSale(30);
        assertNotNull(debitSale.getTransactionToken());

        Transaction debitAuth = debitAuth(50);
        assertNotNull(debitAuth.getTransactionToken());

        Transaction response = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(30),
                new BigDecimal(80)
        ).execute(configName);
        assertNotNull(response);

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());

        if(summary.getResponseCode().equals("580")) {
            LinkedList<String> tokens = new LinkedList<String>();
            tokens.add(creditSale.getTransactionToken());
            tokens.add(creditAuth.getTransactionToken());
            tokens.add(debitSale.getTransactionToken());
            tokens.add(debitAuth.getTransactionToken());

            BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
            assertNotNull(newSummary);
            assertEquals(4, newSummary.getResentTransactions().size());
            for(Transaction re: newSummary.getResentTransactions()) {
                assertEquals("000", re.getResponseCode());
            }
        }
        else {
            assertTrue(summary.getResponseCode().equals("500") || summary.getResponseCode().equals("501"));
        }
    }

    @Test
    public void test_240_batchClose_EndOfShift() throws ApiException {
        configName = "default";

        creditSale(10);
        creditSale(10);
        creditSale(15);
        debitSale(5);
        debitSale(10);

        Transaction response = BatchService.closeBatch(BatchCloseType.EndOfShift)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
        assertTrue(response.getBatchSummary().isBalanced());
    }

    @Test
    public void test_request_to_balance_002() throws ApiException {
        configName = "default";

        visa_swipe_sale_reversal(new BigDecimal(20));
        visa_swipe_sale_return(new BigDecimal(20));
        visa_swipe_sale_return(new BigDecimal(20));
        visa_swipe_sale_void(new BigDecimal(20));
        discover_swipe_sale(new BigDecimal(20));
        discover_swipe_sale(new BigDecimal(20));
       // mastercard_authCapture(new BigDecimal(10));

        DE123_ReconciliationTotals_nws totals = new DE123_ReconciliationTotals_nws();


        totals.setTotalCredits(3,new BigDecimal(80), "VI");
        totals.setTotalCredits(2,new BigDecimal(40), "OH");
        //totals.setTotalCredits(1,new BigDecimal(10), "MC");
        totals.setTotalReturns(2,new BigDecimal(40), "CT",PaymentMethodType.Credit);
        totals.setTotalVoid(1,new BigDecimal(20), "CT", PaymentMethodType.Credit);


        Transaction response = BatchService.closeBatch( BatchCloseType.EndOfShift, batchProvider.getBatchNumber(),
                        batchProvider.getSequenceNumber(),8, totals)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
        assertTrue(response.getBatchSummary().isBalanced());
    }

    @Test
    public void test_request_to_balance_debit_002() throws ApiException {
        configName = "default";

        visa_debit_sale(new BigDecimal(20));
        visa_debit_sale_return(new BigDecimal(20));
        visa_debit_sale_reversal(new BigDecimal(20));
        visa_debit_sale_void(new BigDecimal(20));

        DE123_ReconciliationTotals_nws totals = new DE123_ReconciliationTotals_nws();

        totals.setTotalDebits(3,new BigDecimal(60), "VI");
        totals.setTotalReturns(1, new BigDecimal(20),"DB",PaymentMethodType.Debit);
        totals.setTotalVoid(1, new BigDecimal(20),"DB",PaymentMethodType.Debit);

        Transaction response = BatchService.closeBatch( BatchCloseType.EndOfShift, batchProvider.getBatchNumber(),
                        batchProvider.getSequenceNumber(),5, totals)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
        assertTrue(response.getBatchSummary().isBalanced());
    }


    private Transaction creditAuth(double amount) throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        AuthorizationBuilder builder = track.authorize(new BigDecimal(amount))
                .withCurrency("USD");

        if(configName.equals("NoBatch")) {
            builder.withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber());
        }

        Transaction response = builder.execute(configName);
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
        return capture;
    }
    private Transaction creditSale(double amount) throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        AuthorizationBuilder builder = track.charge(new BigDecimal(amount))
                .withCurrency("USD");

        if(configName.equals("NoBatch")) {
            builder.withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber());
        }

        Transaction response = builder.execute(configName);
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        return response;
    }

    private Transaction debitAuth(double amount) throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");

        AuthorizationBuilder builder = track.authorize(new BigDecimal(amount))
                .withCurrency("USD");

        if(configName.equals("NoBatch")) {
            builder.withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber());
        }

        Transaction response = builder.execute(configName);
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
        return capture;
    }
    private Transaction debitSale(double amount) throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");

        AuthorizationBuilder builder = track.charge(new BigDecimal(amount))
                .withCurrency("USD");

        if(configName.equals("NoBatch")) {
            builder.withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber());
        }

        Transaction response = builder.execute(configName);
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        return response;
    }
    public void visa_swipe_sale(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.VisaSwipe();
        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        assertEquals("000", response.getResponseCode());
    }

    public void visa_swipe_sale_void(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.VisaSwipe();
        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction response1 = response.voidTransaction()
                .execute();
        assertNotNull(response1);

        // check response
        assertEquals("000", response.getResponseCode());
    }

    public void visa_swipe_sale_return(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.VisaSwipe();
        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction response1 = response.refund(20)
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);

        // check response
        assertEquals("000", response.getResponseCode());
    }

    public void visa_swipe_sale_reversal(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        CreditTrackData track = TestCards.VisaSwipe();
        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction reversal = response.reverse(new BigDecimal(20))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        // check response
        assertEquals("000", response.getResponseCode());
    }
    public void discover_swipe_sale(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.DiscoverSwipe();
        Transaction response = track.charge(amount)
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

    public void visa_debit_sale(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");


//        CreditTrackData track = TestCards.VisaSwipe();
        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

    }

    public void visa_debit_sale_void(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction response1 = response.voidTransaction()
                .execute();
        assertNotNull(response1);

    }

    public void visa_debit_sale_return(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction response1 = response.refund(20)
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);

    }

    public void visa_debit_sale_reversal(BigDecimal amount) throws ApiException {
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);

        Transaction reversal = response.reverse(new BigDecimal(20))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
    }


    public void mastercard_authCapture(BigDecimal amount) throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        CreditTrackData track = TestCards.MasterCardSwipe();
        Transaction response = track.authorize(amount)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
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

    @Test
    public void test_240_batchClose_Forced() throws ApiException {
        configName = "default";
        config.setBatchProvider(null);
        CreditTrackData track = TestCards.VisaSwipe();
        Transaction resp = creditSale(10);

        Transaction response = BatchService.closeBatch(BatchCloseType.Forced)
                .withPaymentMethod(track)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
        assertTrue(response.getBatchSummary().isBalanced());
    }

}
