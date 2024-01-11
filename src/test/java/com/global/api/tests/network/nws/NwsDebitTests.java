package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsDebitTests {
    private DebitTrackData track;

    private NetworkGatewayConfig config;

    private AcceptorConfig acceptorConfig;

    public NwsDebitTests() throws ApiException {
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
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
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

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        // debit card
        track = new DebitTrackData();
        track.setValue(";6090001234567891=2112120000000000001?");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_149_pre_authorization() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_217_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_219_swipe_sale_cash_back() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(5))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("090800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check result
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_150_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
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

        // sale data-collect
        Transaction capture = response.preAuthCompletion(new BigDecimal(10))
                .execute();
        assertNotNull(capture);
        assertNull(capture.getPreAuthCompletion());

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("1379", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());
    }

    @Test
    public void test_151_sale_with_surcharge() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge, new BigDecimal(2))
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
    public void test_152_sale_with_cashBack() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
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
    public void test_153_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("200008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_154_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        
        Transaction voidResponse = response.voidTransaction().execute();

        // check message data
        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_155_reverse_authorization() throws ApiException {
        try {
            track.authorize(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
            Assert.fail("Did not throw a timeout");
        }
        catch(GatewayTimeoutException exc) {
             assertEquals(1, exc.getReversalCount());
             assertEquals("400", exc.getReversalResponseCode());
        }
    }

    @Test
    public void test_156_reverse_sale() throws ApiException {
        try {
            track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
            Assert.fail("Did not throw a timeout");
        }
        catch(GatewayTimeoutException exc) {
            assertEquals(1, exc.getReversalCount());
            assertEquals("400", exc.getReversalResponseCode());
        }
    }

    @Test
    public void test_157_reverse_sale_cashBack() throws ApiException {
        try {
            track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withCashBack(new BigDecimal(3))
                    .execute();
            Assert.fail("Did not throw a timeout");
        }
        catch(GatewayTimeoutException exc) {
            assertEquals(1, exc.getReversalCount());
            assertEquals("400", exc.getReversalResponseCode());
        }
    }

    @Test
    public void test_158_refund_reverse() throws ApiException{
    	try {
            track.refund(new BigDecimal(10))
                    .withCurrency("USD")
                    .execute();
            Assert.fail("Did not throw a timeout");
        }
    	catch(GatewayTimeoutException exc) {
            assertEquals(1, exc.getReversalCount());
            assertEquals("400", exc.getReversalResponseCode());
        }
    }
    
    @Test
    public void test_159_ICR_authorization() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        // test case 160
        Transaction capture = response.capture(response.getAuthorizedAmount())
                .execute();
        assertNotNull(capture);
        assertNotNull(capture.getPreAuthCompletion());

        // check the pre-auth completion
        pmi = capture.getPreAuthCompletion().getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check data-collect
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }

//    @Test
//    public void test_161_ICR_reverse_authorization() throws ApiException {
//        try {
//            track.authorize(new BigDecimal(10))
//                    .withCurrency("USD")
//                    .withForceGatewayTimeout(true)
//                    .execute("ICR");
//            Assert.fail("Did not throw a timeout");
//        }
//        catch(GatewayTimeoutException exc) {
//            assertEquals(1, exc.getReversalCount());
//            assertEquals("400", exc.getReversalResponseCode());
//        }
//    }

    @Test
    public void test_162_ICR_partial_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(110), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertNotNull(response.getAuthorizedAmount());
        
        // test case 160
        Transaction capture = response.capture(response.getAuthorizedAmount())
                .execute();
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_164_ICR_auth_reversal() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        Transaction reversal = response.reverse(new BigDecimal(1))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_165_emv_debit_sale() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue(";4024720012345671=18125025432198712345?");
        track.setPinBlock("AFEC374574FC90623D010000116001EE");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData("82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }
    @Test
    public void test_166_sale_void_with_cashBack() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
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

        Transaction voidResponse = response.voidTransaction().execute();

        // check message data
        PriorMessageInformation vpi = voidResponse.getMessageInformation();
        assertNotNull(vpi);
        assertEquals("1420", vpi.getMessageTransactionIndicator());
        assertEquals("000800", vpi.getProcessingCode());
        assertEquals("441", vpi.getFunctionCode());
        assertEquals("4351", vpi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void test_sale_reversal() throws ApiException {

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
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_purchase() throws ApiException {

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("200008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("200008", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_swipe_reversal_cashBack() throws ApiException {
        Transaction response = track.charge(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
//        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(13))
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_009_swipe_sale_reversal() throws ApiException {
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
        assertEquals("000800", pmi.getProcessingCode());
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
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


    @Test
    public void test_165_emv_debit_sale_codeCoverage() throws ApiException {
        acceptorConfig.setAddress(null);
        ServicesContainer.configureService(config);

        DebitTrackData track = new DebitTrackData();
        track.setValue(";4024720012345671=18125025432198712345?");
        track.setPinBlock("AFEC374574FC90623D010000116001EE");

        BuilderException builderException = assertThrows(BuilderException.class,
                () ->  track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withTagData("82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019")
                .execute());
        assertEquals("Address is required in acceptor config for Debit/EBT Transactions.", builderException.getMessage());



    }

}
