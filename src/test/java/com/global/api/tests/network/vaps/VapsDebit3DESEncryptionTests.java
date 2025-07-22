package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.exceptions.ApiException;
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
import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class VapsDebit3DESEncryptionTests {
    private DebitTrackData debit;
    private AcceptorConfig acceptorConfig;

    public VapsDebit3DESEncryptionTests() throws ApiException {
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
        NetworkGatewayConfig config = new NetworkGatewayConfig();
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
        // DEBIT
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2510");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));
    }

    //------------------------------------debit-----------------------------------------------------
    @Test
    public void test_001_debit_auth() throws ApiException {
        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
    public void test_debit_auth_capture() throws ApiException {
        Transaction response = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

//         check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_014_visa_encrypted_forceRefund_debit_10297() throws ApiException {
        Transaction response1 = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withOfflineAuthCode("105683")
                .execute();
        assertNotNull(response1);
        assertEquals("000", response1.getResponseCode());

        Transaction response2 = response1.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response1.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response2);
        assertEquals("000", response2.getResponseCode());

        Transaction response3 = response2.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(Integer.parseInt(response2.getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(response3);
        assertEquals("000", response3.getResponseCode());

        Transaction response = NetworkService.forcedRefund(response3.getTransactionToken())
                .withCurrency("USD")
                .withForceToHost(true)
                .withPaymentMethod(debit)
                .execute();
        assertNotNull(response);

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_debit_sale_with_cashBack() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("090800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_008_debit_sale() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_014_debit_encrypted_refund() throws ApiException {
        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_014_debit_encrypted_forceRefund() throws ApiException {
        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction resubmit = NetworkService.forcedRefund(response.getTransactionToken())
                .withForceToHost(true)
                .withPaymentMethod(debit)
                .execute();
        assertNotNull(resubmit);
        assertEquals("000", resubmit.getResponseCode());
    }

    @Test
    public void test_015_debit_swipe_void() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction()
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
    public void test_013_debit_encrypted_reversal() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromNetwork(
                response.getAuthorizedAmount(),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
                debit,
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
    public void test_001_sale_refund() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction response1 = response.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);
        assertEquals("000", response1.getResponseCode());
    }

    @Test
    public void test_008_debit_sale1() throws ApiException {
        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }


    @Test
    public void preauthCompl() throws ApiException {
        Transaction recreated = Transaction.fromNetwork(
                new BigDecimal(10),
                "135425",
                new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized),
                debit,
                "1220",
                "000073",
                "240327043953",
                "008100"
        );

        Transaction preAuth = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertEquals("000", preAuth.getResponseCode());
    }

    @Test
    public void test_debit_refund_retry() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withSystemTraceAuditNumber(response.getSystemTraceAuditNumber())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(response.getNtsData())
                .withPosDataCode(response.getPosDataCode())
                .withMessageTypeIndicator(response.getMessageTypeIndicator())
                .withProcessingCode(response.getProcessingCode())
                .withTransactionTime(response.getOriginalTransactionTime())
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_debit_pre_auth_retry() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction authResponse = debit.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(authResponse);
        assertEquals("000", authResponse.getResponseCode());

        Transaction captureResponse = authResponse.preAuthCompletion()
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(captureResponse.getAuthorizedAmount())
                .withSystemTraceAuditNumber(captureResponse.getSystemTraceAuditNumber())
                .withAuthorizationCode(captureResponse.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(captureResponse.getNtsData())
                .withPosDataCode(captureResponse.getPosDataCode())
                .withMessageTypeIndicator(captureResponse.getMessageTypeIndicator())
                .withProcessingCode(captureResponse.getProcessingCode())
                .withTransactionTime(captureResponse.getOriginalTransactionTime())
                .build();


        Transaction preAuth = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(preAuth);
        assertEquals("000", preAuth.getResponseCode());
    }

    @Test
    public void test_debit_sale_retry_issue_10295() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2410");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withSystemTraceAuditNumber(response.getSystemTraceAuditNumber())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withPaymentMethod(debit)
                .withNtsData(response.getNtsData())
                .withPosDataCode(response.getPosDataCode())
                .withMessageTypeIndicator(response.getMessageTypeIndicator())
                .withProcessingCode(response.getProcessingCode())
                .withTransactionTime(response.getOriginalTransactionTime())
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

    }

    @Test
    public void test_debit_sale_issue_10300() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());


    }

    @Test
    public void test_debit_sale_retry_issue_10300() throws ApiException {
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2412");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));


        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(new BigDecimal(10))
                .withSystemTraceAuditNumber("009357")
                .withAuthorizationCode("86    ")
                .withPaymentMethod(debit)
                .withNtsData(new NtsData(FallbackCode.Received_IssuerTimeout, AuthorizerCode.Terminal_Authorized))
                .withPosDataCode("V10101B1014C")
                .withMessageTypeIndicator("1200")
                .withProcessingCode("000800")
                .withTransactionTime("240410023005")
                .build();

        Transaction capture = recreated.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_10315_batchClose_retransmitDataCollect_withBatchSummary_debit() throws ApiException {
        BatchProvider batchProvider = BatchProvider.getInstance();
        String configName = "default";
        acceptorConfig.setHardwareLevel("S3");

        Transaction creditSale = debit.charge(new BigDecimal(10))
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

    //Negative scenerio with incorrect operation Type
    @Test
    public void test_debit_sale_operationType() throws ApiException {
        acceptorConfig.setOperationType(OperationType.Reserved);
        debit = new DebitTrackData();
        debit.setCardType("PINDebitCard");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setTrackNumber(TrackNumber.TrackTwo);
        debit.setEncryptedPan("3A2067D00508DBE43E3342CC77B0575E");
        debit.setExpiry("2410");

        debit.setEncryptionData(EncryptionData.setKSNAndEncryptedData("3EC0C41AB0CCC3BCA6EF798140BEF7BB5A06F78222AFD7BA8E949CA21AAF26E3EB2A4334BE31534E",
                "F000019990E00003"));

        Transaction response = debit.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check response
        assertTrue(("952").matches(response.getResponseCode()));
    }
}