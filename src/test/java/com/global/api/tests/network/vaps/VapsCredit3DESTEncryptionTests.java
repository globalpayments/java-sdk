package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE48_1_CommunicationDiagnostics;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.terminals.TerminalUtilities;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsCredit3DESTEncryptionTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private  AcceptorConfig acceptorConfig ;
    private  NetworkGatewayConfig config ;

    public VapsCredit3DESTEncryptionTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("S3");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        track = new CreditTrackData();
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        track.setCardType("MC");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setTrackNumber(TrackNumber.TrackOne);
        track.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F");
        track.setExpiry("2510");

        // VISA
        card = new CreditCardData();
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E00003"));
        card.setCardType("MC");
        card.setExpYear(2024);
        card.setExpMonth(12);

        cardWithCvn = new CreditCardData();
        cardWithCvn.setCvn("103");
        cardWithCvn.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E00003"));
        cardWithCvn.setCardType("MC");
        cardWithCvn.setCardPresent(false);
        cardWithCvn.setReaderPresent(false);
        cardWithCvn.setExpMonth(10);
        cardWithCvn.setExpYear(2025);
    }

    //-----------------------------------------------Credit-------------------------------------------
    @Test
    public void test_001_credit_manual_auth_cvn() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth_mc_indicator() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withMasterCardIndicator(MasterCardCITMITIndicator.CARDHOLDER_INITIATED_SUBSCRIPTION)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_swipe_auth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_credit_sale_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_sale_reversal() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_Credit_auth_capture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_credit_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_credit_swipe_forceSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction forceSale = NetworkService.forcedSale(response.getTransactionToken())
                .withForceToHost(true)
                .withPaymentMethod(track)
                .execute();
        assertNotNull(forceSale);
        assertEquals("000", forceSale.getResponseCode());
    }

    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                NtsData.interchangeAuthorized(),
                cardWithCvn
        );
        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_credit_balance_inquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_007_credit_swipe_auth_capture01() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }
    @Test
    public void test_Credit_auth_capture02() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_Credit_auth_capture03() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_009_credit_swipe_sale() throws ApiException {

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        String IRR_data=response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction()
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,IRR_data)
                .withCustomerInitiated(true)
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_010_credit_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_013_visa_encrypted_follow_on() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                track,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = recreated.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_014_visa_encrypted_Refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

    }

    @Test
    public void test_014_visa_encrypted_Refund_cardData() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_001_refund_resubmit_DataCollectforce() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("TYPE04")
                .execute();
        assertNotNull(response);

        NtsData ntsData = new NtsData();
        response.setNtsData(ntsData);
        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
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
    public void test_014_visa_encrypted_forceRefund_10297() throws ApiException {
        Transaction response1 = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response1);

        Transaction response2 = response1.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response1.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response2);

        NtsData ntsData = new NtsData();
        response2.setNtsData(ntsData);
        Transaction response3 = response2.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response2.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response3);

        Transaction response = NetworkService.forcedRefund(response3.getTransactionToken())
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(response);

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_Credit_swipe_voice_capture() throws ApiException {
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
    public void test_015_credit_swipe_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction()
                .withCustomerInitiated(false)
                .execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }
    @Test
    public void test_force_dataCollect() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .withForceToHost(true)
                .execute();
        assertNotNull(captureResponse);

        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void test_001_resubmitDataCollect_issue_10292() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
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
    public void test_001_resubmitDataCollectForce_issue_10292() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture()
                .withCurrency("USD")
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
    public void test_10315_batchClose_retransmitDataCollect_withBatchSummary() throws ApiException {
        BatchProvider batchProvider = BatchProvider.getInstance();
        String configName = "default";
        acceptorConfig.setHardwareLevel("S3");

        Transaction creditSale = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(creditSale);
        assertEquals("000", creditSale.getResponseCode());
        assertNotNull(creditSale.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));


        Transaction response = BatchService.closeBatch(1, new BigDecimal(10), null)
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());


        LinkedList<String> tokens = new LinkedList<>();
        tokens.add(creditSale.getTransactionToken());

        BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
        assertNotNull(newSummary);

        assertTrue(newSummary.getResponseCode().equals("500") || newSummary.getResponseCode().equals("501"));

    }
    @Test //Negative scenerio with invalid KSN value
    public void test_002_credit_manual_auth_with_InvalidData() throws ApiException {
        card.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3A2067D00508DBE43E3342CC77B0575E17401487FC0B377F",
                "F000019990E003"));
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertTrue(("111").matches(response.getResponseCode()));
    }

    /** DE48_1  code coverage */
    @Test
    public void DE48_1_CommDiagnostic_tests_codeCoverage() {
        String original = "2181";

        DE48_1_CommunicationDiagnostics element = new DE48_1_CommunicationDiagnostics()
                .fromByteArray(original.getBytes());
        assertEquals(2, element.getCommunicationAttempts());
        assertEquals(DE48_ConnectionResult.ResponseBufferOverflow, element.getConnectionResult());
        assertEquals(DE48_HostConnected.SecondaryHost, element.getHostConnected());

        byte[] buffer = element.toByteArray();
        assertEquals(original, new String(buffer));
    }

    @Test
    public void test_cardDataInputCapability_codeCoverage() throws ApiException {
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.Manual);

        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
}
