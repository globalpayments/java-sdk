package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.abstractions.IBatchProvider;
import com.global.api.network.abstractions.IStanProvider;
import com.global.api.network.elements.DE48_MessageControl;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsDebitTests {
    private DebitTrackData track;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;

    public VapsDebitTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values DE 22
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values DE 48-33
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setPinlessDebit(false);
        acceptorConfig.setSupportWexAvailableProducts(true);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
//        config.setTerminalId("0003698521408");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");
        config.setTimeout(20000);
        ServicesContainer.configureService(config);

        // with merchant type
//        config.setMerchantType("5542");
//        ServicesContainer.configureService(config, "ICR");

        // debit card
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        //track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_authorization_Debit_withFee_01() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
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
    public void test_Debit_Auth_Capture_02() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(40))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(10))
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        Transaction capture = response.capture(new BigDecimal(10))
                .execute();
        assertNotNull(capture);


        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_05_ready_link_Data_Collect() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("4111111111111111=1225");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("000", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_authorization_withFee() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(1))
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
    public void test_149_pre_authorization() throws ApiException {
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
        assertEquals("000", response.getResponseCode());
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
        assertNotNull(response.getPreAuthCompletion());

        // check the completion
        Transaction capture = response.getPreAuthCompletion();

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
                .withFee(FeeType.Surcharge, new BigDecimal(1))
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
    public void test_154_force_void() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction voidResponse = response.voidTransaction()
                .withForceToHost(true)
                .execute();

        // check message data
        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4356", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_155_reverse_authorization() throws ApiException {
        track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
        Assert.fail("Did not throw a timeout");
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_156_reverse_sale() throws ApiException {
        track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
        Assert.fail("Did not throw a timeout");
    }

    @Test
    public void test_157_reverse_sale_cashBack() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
    }

    @Test
    @Ignore
    public void test_159_ICR_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(12))
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
        assertEquals("000", response.getResponseCode());

        // test case 160
        Transaction capture = response.capture(new BigDecimal(12))
                .execute("ICR");
        assertNotNull(capture);
        assertNotNull(capture.getPreAuthCompletion());

        // check data-collect
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check the pre-auth completion
        pmi = capture.getPreAuthCompletion().getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1379", pmi.getMessageReasonCode());

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_161_ICR_reverse_authorization() throws ApiException {
        track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute("ICR");
        Assert.fail("Did not throw a timeout");
    }

    @Test
    @Ignore
    public void test_162_ICR_partial_authorization() throws ApiException {
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
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        // test case 160
        Transaction capture = response.capture(response.getAuthorizedAmount())
                .execute("ICR");
        assertNotNull(capture);

        // check message data
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
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
        //assertEquals("000", response.getResponseCode());

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
    public void test_166_debit_pre_auth_cancel() throws ApiException {
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
        assertEquals("000", response.getResponseCode());

        Transaction cancel = response.cancel(response.getAuthorizedAmount())
                .execute("ICR");
        assertNotNull(cancel);

        pmi = cancel.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals("400", cancel.getResponseCode());
    }

    @Test
    public void test_167_debit_pre_auth_cancel_fromNetwork() throws ApiException {
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
        assertEquals("000", response.getResponseCode());

        Transaction rebuild = Transaction.fromNetwork(
                new BigDecimal("1"),
                response.getAuthorizationCode(),
                response.getNtsData(),
                track,
                pmi.getMessageTransactionIndicator(),
                pmi.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode());

        Transaction cancel = rebuild.cancel(response.getAuthorizedAmount())
                .execute("ICR");
        assertNotNull(cancel);

        pmi = cancel.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals("400", cancel.getResponseCode());
    }

    @Test
    public void test_168_debit_pre_auth_completion() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(12))
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
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(12))
                .execute("ICR");
        assertNotNull(capture);
        assertNotNull(capture.getPreAuthCompletion());

        // check the pre-auth completion
        pmi = capture.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1376", pmi.getMessageReasonCode());

        // check response
        assertEquals("000", capture.getResponseCode());

        // check the data-collect
        Transaction completion = capture.getPreAuthCompletion();
        assertNotNull(completion);

        if (!completion.getResponseCode().equals("000")) {
            // re-do the data-collect
            completion = response.preAuthCompletion(new BigDecimal(12))
                    .execute("ICR");

            assertNotNull(completion);
        }

        pmi = completion.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("1379", pmi.getMessageReasonCode());

        assertEquals("000", completion.getResponseCode());
    }

    @Test
    @Ignore
    public void test_168_debit_sale_failed_data_collect() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
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

        Transaction completion = response.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(completion);
    }

    @Test
    @Ignore
    public void test_169_debit_pre_auth_manual() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();

        Transaction response = track.authorize(new BigDecimal("1"), true)
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction completion = response.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(completion);
        assertEquals("000", completion.getResponseCode());
    }

    @Test
    @Ignore
    public void test_169_debit_pre_auth_manual_builder() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();

        Transaction response = track.authorize(new BigDecimal("1"), true)
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("1"))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withNtsData(response.getNtsData())
                .withPaymentMethod(track)
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator())
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber())
                .withTransactionTime(response.getOriginalTransactionTime())
                .withProcessingCode(response.getProcessingCode())
                .build();

        Transaction capture = rebuild.capture()
                .withSystemTraceAuditNumber(stan.generateStan())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction completion = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .execute();
        assertNotNull(completion);
        assertEquals("000", completion.getResponseCode());
    }

    @Test
    public void test_170_reverse_sale_with_cashBack() throws ApiException {
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

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCashBackAmount(new BigDecimal(3))
                .withTimestamp(response.getTimestamp())
                .withTerminalError(true)
                .withUniqueDeviceId("123456789")
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_171_ReadyLink() throws ApiException {
        CreditTrackData rlTrack = new CreditTrackData();
        rlTrack.setValue("354358770862127311=210612100000439000");

        Transaction response = rlTrack.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_172_DebitPartialCashBackReversal() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(10))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction cancel = response.cancel(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(10))
                .execute();
        assertNotNull(cancel);
        assertEquals("400", cancel.getResponseCode());
    }

    @Test
    public void test_173_DE30_test() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction transaction = Transaction.fromBuilder()
                .withAmount(new BigDecimal(10))
                .withAuthorizedAmount(response.getAuthorizedAmount())
                .withSystemTraceAuditNumber(response.getSystemTraceAuditNumber())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withPaymentMethod(track)
                .withNtsData(response.getNtsData())
                .withPosDataCode(response.getPosDataCode())
                .withMessageTypeIndicator(response.getMessageTypeIndicator())
                .withProcessingCode(response.getProcessingCode())
                .withTransactionTime(response.getOriginalTransactionTime())
                .build();

        Transaction retryResponse = transaction.preAuthCompletion(response.getAuthorizedAmount())
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.SwipeIndicator, "0")
                .withPriorMessageInformation(response.getMessageInformation())
                .execute();
        assertNotNull(retryResponse);
        assertEquals("000", retryResponse.getResponseCode());
    }

    @Test(expected = GatewayException.class)
    public void test_174_InvalidDebitAuthorizer() throws ApiException {
        String ntsString = "00 30";
        NtsData.fromString(ntsString);
    }

    @Test
    public void test_175_encrypted_debit_sale() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setEncryptedPan("4355561117063338");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_175_PinDebit_Partial_Amount_Auth_Capture() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(40), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .execute("ICR");
        assertNotNull(capture);

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_176_pre_authorization_with_fee_amount() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge, new BigDecimal(11))
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
    public void test_177_reverse_sale_cashBack_with_fee() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge, new BigDecimal(5))
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    @Ignore
    public void test_PinDebit_Partial_Amount_Auth_Capture() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(40),true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());
        response.setNtsData(new NtsData());
        Transaction capture = response.capture(20)
                .execute("ICR");
        assertNotNull(capture);

        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_169_pindebit_1221_datacollect() throws ApiException {
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction capture = NetworkService.resubmitDataCollect(response.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_reverse_sale_cashBack_code_coverage_only() throws ApiException {
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        //invalid pin block
        track.setPinBlock("00000000000000000000000000000000");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        assertEquals("126",response.getResponseCode());

        Transaction reversal = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        assertEquals("000", reversal.getResponseCode());

    }
    @Test
    public void test_DataCollect_Individual_CodeCoverageOnly() throws ApiException {
        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("1"))
                .withAuthorizedAmount(new BigDecimal(10))
                .withAuthorizationCode("777666")
                .withNtsData(new NtsData())
                .withPaymentMethod(track)
                .withMessageTypeIndicator("1100")
                .withSystemTraceAuditNumber("001996")
                .withTransactionTime("090540")
                .withProcessingCode("000800")
                .build();

        GatewayException pinBlockError = assertThrows(GatewayException.class,
                () -> rebuild.capture(new BigDecimal(10))
                        .withCurrency("USD")
                        .execute());

        assertEquals("Unexpected response from gateway: 70 FormatError", pinBlockError.getMessage());
    }
    @Test
    public void test_entry_method_proximity_code_coverage() throws ApiException {
        track.setEntryMethod(EntryMethod.Proximity);
        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge,new BigDecimal(11))
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
    public void test_entry_method_address_null_code_coverage() throws ApiException {

        acceptorConfig.setAddress(null);
        config.setAcceptorConfig(acceptorConfig);

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
                     track.authorize(new BigDecimal(1))
                    .withCurrency("USD")
                    .withFee(FeeType.Surcharge,new BigDecimal(11))
                    .execute();
        });
        assertEquals("Address is required in acceptor config for Debit/EBT Transactions.", builderException.getMessage());

    }
    @Test
    public void test_currencyCode_code_coverage() throws ApiException {
        track.setEntryMethod(EntryMethod.Proximity);
        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("CAD")
                .withFee(FeeType.Surcharge,new BigDecimal(11))
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000800", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        Transaction capture = response.capture(response.getAuthorizedAmount())
                .withCurrency("CAD")
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }
    @Test
    public void test_reversal_with_additional_amount_coverage_only() throws ApiException {
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(10))
                .execute();
        assertNotNull(reversal);

        assertEquals("000", reversal.getResponseCode());
    }

    @Test
    public void test_resubmit_token_null_code_coverage() throws ApiException {
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        BuilderException builderException = assertThrows(BuilderException.class, ()->{
            NetworkService.resubmitDataCollect(null)
                    .withForceToHost(true)
                    .execute();
        });
        assertEquals("The transaction token cannot be null for resubmitted transactions.", builderException.getMessage());
    }

    @Test
    public void test_resubmit_dataCollect_code_coverage_only() throws ApiException {
        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);

        Transaction resubmitResponse = NetworkService.resubmitDataCollect(capture.getTransactionToken()).
                execute();
        assertEquals("000",resubmitResponse.getResponseCode());
    }
    @Test
    public void test_billng_address_dataCollect_code_coverage_only() throws ApiException {

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


        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        assertEquals("000", response.getResponseCode());
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
        address.setType(AddressType.Shipping);

        acceptorConfig.setAddress(address);
        config.setAcceptorConfig(acceptorConfig);


        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        assertEquals("000",response.getResponseCode());
    }
    @Test
    public void test_reversal_withCashBack_code_coverage_only() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);

       TransactionReference reference = response.getTransactionReference();
       reference.setOriginalApprovedAmount(null);
       response.setTransactionReference(reference);

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
    }

    @Test
    public void test_sale_without_internal_datacollect() throws ApiException {
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
    }

    @Test
    public void test_sale_internal_datacollect() throws ApiException {
        IStanProvider stanGenerator = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        int stan = stanGenerator.generateStan();
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan,stan)
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
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
    public void test_Debit_Auth_without_internal_Capture() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(40))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(10))
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        Transaction capture = response.capture(new BigDecimal(10))
                .execute();
        assertNotNull(capture);


        // check response
        assertEquals("000", capture.getResponseCode());
    }
    @Test
    public void test_Debit_Auth_with_internal_Capture() throws ApiException {
        IStanProvider stanGenerator = StanGenerator.getInstance();
        int stan = stanGenerator.generateStan();

        Transaction response = track.authorize(new BigDecimal(40))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee, new BigDecimal(10))
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("000", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        Transaction capture = response.capture(new BigDecimal(10))
                .withSystemTraceAuditNumber(stan,stan)
                .execute();
        assertNotNull(capture);


        // check response
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_debit_pre_auth_builder() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");
        //De 48-14 set internally by SDK
        //DE 46 Mandatory when fee has been accessed at POS.
        //DE 62 NPC set internally by SDK
        Transaction response = track.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                //DE 3 internally set by SDK
                //DE 24 internally set by SDK
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing error if not passing.(This might be because we received RC 126)
                .withPaymentMethod(track) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withBatchNumber(batch.getBatchNumber())
                .withSequenceNumber(batch.getSequenceNumber())
                .build();

        Transaction capture = authResponse.capture(new BigDecimal("15"))
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_debit_resubmit() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");
        //De 48-14 set internally by SDK
        //DE 46 Mandatory when fee has been accessed at POS.
        Transaction response = track.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData())
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withBatchNumber(batch.getBatchNumber())
                .withPaymentMethod(track) //Original Payment method
                .withSequenceNumber(batch.getSequenceNumber())
                .build();

        Transaction capture = authResponse.capture(new BigDecimal("10"))
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withForceToHost(true)
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }


    @Test
    public void test_debit_sale() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        DateTime date = DateTime.now();

        track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date.toString("yyMMddhhmmss"))
                .withFee(FeeType.Surcharge, new BigDecimal("10"))
                .withUniqueDeviceId("101")
                .withClerkId("1211")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(response);
    }

    @Test
    public void test_debit_preAuth_completion_AuthProcessing() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        //De 48-14 set internally by SDK
        //DE 46 Mandatory when fee has been accessed at POS.
        //DE 62 NPC set internally by SDK
        Transaction response = track.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                //DE 3 internally set by SDK
                //DE 24 internally set by SDK
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData())
                .withPaymentMethod(track) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withBatchNumber(batch.getBatchNumber())
                .withSequenceNumber(batch.getSequenceNumber())
                .build();

        Transaction dataCollect = authResponse.capture(new BigDecimal("10"))
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(dataCollect);
        assertEquals("000", dataCollect.getResponseCode());

        authResponse = Transaction.fromBuilder()
                //DE 3 internally set by SDK
                //DE 24 internally set by SDK
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData())
                .withPaymentMethod(track) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withProcessingCode(response.getProcessingCode())
                .build();

        Transaction completion = authResponse.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withBatchNumber(dataCollect.getTransactionReference().getBatchNumber(),
                        dataCollect.getTransactionReference().getSequenceNumber())
                .withPriorMessageInformation(dataCollect.getMessageInformation()) //DE 48-39 M
                .execute();
        assertNotNull(completion);
    }

    public Transaction test_debit_Authorization(BigDecimal amount) throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("YYMMDDhhmmss");

        return track.authorize(new BigDecimal("1"), true)
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withFee(FeeType.Surcharge,new BigDecimal("10"))
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .withUniqueDeviceId("101")
                .withClerkId("1211")
                .execute();
        }

    @Test
    public void test_debit_refund() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction refundResponse = track.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber())
                .execute();
        assertNotNull(refundResponse);

        // check response
        assertEquals("000", refundResponse.getResponseCode());
        PriorMessageInformation pmi = refundResponse.getMessageInformation();

        Transaction recreated = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original amount
                .withAuthorizationCode(refundResponse.getAuthorizationCode()) // auth code from response DE 38 M
                .withNtsData(refundResponse.getNtsData())
                .withPaymentMethod(track)
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator())
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(refundResponse.getOriginalTransactionTime())
                .withProcessingCode(refundResponse.getProcessingCode())
                .withBatchNumber(refundResponse.getTransactionReference().getBatchNumber())
                .withSequenceNumber(refundResponse.getTransactionReference().getSequenceNumber())
                .build();

        Transaction capture = recreated.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan())
                .withPriorMessageInformation(pmi) //DE 48-39
                .withTimestamp(date) //DE 12 - M
                .execute();
        assertNotNull(capture);
    }

    @Test
    public void test_debit_sale_capture() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = track.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1001")
                .withChipCondition(null)
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withNtsData(saleResponse.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing
                .withPaymentMethod(track) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber())
                .withSequenceNumber(saleResponse.getTransactionReference().getSequenceNumber())
                .build();

        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_debit_sale_capture_partialApproval() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = track.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("1234") //DE 62
                .withChipCondition(null)
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withAuthorizedAmount(saleResponse.getAuthorizedAmount()) //Approved Amount
                .withNtsData(saleResponse.getNtsData())
                .withPaymentMethod(track) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();


        Transaction capture = rebuild.preAuthCompletion(rebuild.getTransactionReference().getOriginalApprovedAmount())
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),
                        saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

//    auth-capture
    @Test
    public void test_debit_resubmit_auth_capture() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");
        //De 48-14 set internally by SDK
        //DE 46 Mandatory when fee has been accessed at POS.
        //DE 62 NPC set internally by SDK
        Transaction response = track.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                //DE 3 internally set by SDK
                //DE 24 internally set by SDK
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing error if not passing.(This might be because we received RC 126)
                .withPaymentMethod(track) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withBatchNumber(batch.getBatchNumber())
                .withSequenceNumber(batch.getSequenceNumber())
                .build();

        Transaction capture = authResponse.capture(new BigDecimal("15"))
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withForceToHost(true)
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }

    @Test
    public void test_debit_resubmit_sale_capture() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");

        Transaction saleResponse = track.charge(new BigDecimal("10")) //Amount : DE 4
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan()) //STAN : DE 11
                .withBatchNumber(batch.getBatchNumber(), batch.getSequenceNumber()) // DE 48-4
                .withTimestamp(date) // DE 12
                .withUniqueDeviceId("2024") //DE 62
                .withChipCondition(null)
                .execute();
        assertNotNull(saleResponse);

        PriorMessageInformation pmi = saleResponse.getMessageInformation();

        Transaction rebuild = Transaction.fromBuilder()
                .withAmount(new BigDecimal("10")) //original transaction amount
                .withAuthorizationCode(saleResponse.getAuthorizationCode()) // auth code from sales response DE 38 M
                .withAuthorizedAmount(saleResponse.getAuthorizedAmount()) //Approved Amount
                .withNtsData(saleResponse.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing error if not passing.(This might be because we received RC 126)
                .withPaymentMethod(track) //Original Payment method DE 48-11 M
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) // original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) // Original STAN DE 56 M
                .withTransactionTime(saleResponse.getOriginalTransactionTime()) // original transaction time DE 56 M
                .withProcessingCode(saleResponse.getProcessingCode()) //original Processing code DE 3 M
                .build();


        Transaction capture = rebuild.preAuthCompletion()
                .withSystemTraceAuditNumber(stan.generateStan()) //DE 11
                .withTimestamp(date)    //DE 12 - M
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .withBatchNumber(saleResponse.getTransactionReference().getBatchNumber(),saleResponse.getTransactionReference().getSequenceNumber())
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(capture.getTransactionToken())
                .withTimestamp(date)
                .execute();
        assertNotNull(resubmitDataCollect);
    }

    @Test
    public void test_debit_force_DataCollect() throws ApiException {
        IStanProvider stan = StanGenerator.getInstance();
        IBatchProvider batch = BatchProvider.getInstance();
        String date = DateTime.now().toString("yyMMddhhmmss");
        //De 48-14 set internally by SDK
        //DE 46 Mandatory when fee has been accessed at POS.
        //DE 62 NPC set internally by SDK
        Transaction response = track.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withUniqueDeviceId("2402")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        PriorMessageInformation pmi = response.getMessageInformation();

        Transaction authResponse = Transaction.fromBuilder()
                //DE 3 internally set by SDK
                //DE 24 internally set by SDK
                .withAmount(new BigDecimal("10")) //Original Amount
                .withAuthorizationCode(response.getAuthorizationCode()) // Auth Code from response
                .withNtsData(response.getNtsData()) //DE 62-NTS Need to check, as we are receiving this parameter missing error if not passing.(This might be because we received RC 126)
                .withPaymentMethod(track) //Original Payment method
                .withMessageTypeIndicator(pmi.getMessageTransactionIndicator()) //Original MTI DE 56 M
                .withSystemTraceAuditNumber(pmi.getSystemTraceAuditNumber()) //Original Stan DE 56 M
                .withTransactionTime(response.getOriginalTransactionTime()) //Original Transaction Time DE 56 M
                .withBatchNumber(batch.getBatchNumber())
                .withSequenceNumber(batch.getSequenceNumber())
                .build();

        Transaction capture = authResponse.capture(new BigDecimal("15"))
                .withSystemTraceAuditNumber(stan.generateStan())
                .withTimestamp(date)
                .withForceToHost(true) //For Force DataCollect DE 25
                .withPriorMessageInformation(pmi) //DE 48-39 M
                .execute();
        assertNotNull(capture);
        assertEquals("000", capture.getResponseCode());
    }

    @Test
    public void test_visaReadyLink_Data_Collect_withCardType_completion_order1() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=25121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction dataCollectResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(dataCollectResponse);
        // check response
        assertEquals("000", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_visaReadyLink_Data_Collect_withCardType_completion_order2() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setCardType("VisaReadyLink");
        track.setValue(";4009081122223335=25121010000012345678?");

        Transaction response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction dataCollectResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(dataCollectResponse);
        // check response
        assertEquals("000", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_token_testing_PKCS5_NoPadding() throws ApiException {
        String date = DateTime.now().toString("yyMMddhhmmss");
        List<String> tokenArr = new ArrayList<>();
        tokenArr.add("6q3vD8flL4Lf1nicXedQK360Rg5pcUUEiFwVv8uX1o4cOofAP/qzEmaAAQiyljiO1ksiDM2IdC59r5PwcSjsEN9NsyKOkjX31kp3KsBK7be3dt/5qg/X84ojh4/g1rtvttbvjbeM733KvMfa3U4t/9w3XuCY5ngMiGmlEVy19g2jr/4RGCBwqmVqCBCdeORQgZiZSYYDRIFDyXGhhUzZxkVeA7K3PD7KYIOXiqs8z0zDU6n/7Ux+868Zn161ZfUfqM71bAyMBR9a2tZOxioRQFSyNj3Ez0EhMY+vI17BAjzHXOvNCuHagQop5sh65F6THg14e/4PWE8wHjp9p33Z4o0uumSxrQWrQDBBK70e2Sgg4FbHDsFXutZxR/v/FdU7uIO49p9SsajD5rHXS6xVu/AhZc24857LM7WShy07nG3pVZ1q8D/qm/yL98SIa1boMjPrEPFQE1egaIx8vRxdO3nkeLPTyjoxE3MQWJq7ycBn6AyBnQ1Lfl4joqU2j4TOr2yRS5cpzLB4+Sp5a6W8v98mPDR3n2f9ZN6AXw579uu5UXIjVT4GnOV3Wfk/pCbkJruJDiHLqoAcAvNPzWSTnTLSeoUwrVHOYXwl23xzdDc6PHVJJ4J2CC1y7Cn/as5UbA1JrQ==");
        tokenArr.add("6q3vD8flL4Lf1nicXedQK360Rg5pcUUEiFwVv8uX1o4cOofAP/qzEmaAAQiyljiO1ksiDM2IdC59r5PwcSjsEN9NsyKOkjX31kp3KsBK7f21Zs/4qg/X84ojh43g1rtvttbvjbeM73LIrNfV3U4t/9w3XuCY5ngMiGmlEVy19g2jr/4RGCBwqmVqCBCdeORQgZiZSYYDRIFDyXGhhUzZxkVeA7K3PD7KYIOXiqs8z0zDU6n/7Ux+868Zn161ZfUfqM71bAyMBR9a2tZOxioRQFSyNj3Ez0EhMY+vI17BAjzHXOvNCuHagQop5sh65F6THg14e/4PWE8wHjp9p33Z4o0uumSxrQWrQDBBK70e2Sgg4FbHDsFXutZxR/v/FdU7uIO49p9SsajD5rHXS6xVu/AhZc24857LM7WShy07nG3pVZ1r8i/qm/yL98SIa1boMjPrEPFQE1egaIx8vRxdO3nkeLPTyjoxE3MQWJq7ycBn6AyBnQ1Lfl4joqU2j4TMr2yRS5cpzLB4+Sp5a6W8vd8mPDR3n2f9ZN6AUAxr5uS5UXIjVT4GnOV3Wfk/pCbkJruJDiHLqoAcAvNPzWSTnTLSeoUwrVHOYXwl23xzdDc6PHVJGp8wtSNOnlEd0wIHVIzfdw==");
        tokenArr.add("6q3vD8flL4Lf1micXedQK360Rg5pcUUEiFwVv8uX1o4cOofAP/qzEmaAAQiyljiO1ksiDM2IdC59r5PwcSjsEN9NsyKOkjX31kp3KsBK7f21WMv6qg/X84ojh4vg1rtvttbvjbeM73LIktPX3U4t/9w3XuCY5ngMiGmlEVy19g2jr/4RGCBwqmVqCBCdeORXg5iBWYIERJFDyXHphkvZ1kFZA7K3PD6OYpO9yak82wrCfbXg7Ux+pKwY71uyYPottbz+XRmMAUtB2NYd3F8WIkiNJRDVyTFLKY+nTkPrO1zeKejzH+begRc8zMh85GenGg5gbOsOCiEzGEJksljnloErsmayrQWrRBlFNaNr53w74lniCsZ16cpxR933GN0NtIa4vZxFsbjDyLLrV4FB7/Axfc2m5rTLM7C4mSoVti/rRbdv8D75r/iM8PmUaFb4MjPzAOlCOUekaqZ9vhx3OnvaQv7R9SkFF3QQWJqFyYNnxhiOnyNPOl4zqqU2j5CEr0KBRJcH3LB4jV5Qd7W498UaBQNto06Uft/8ZxZqgtOhJVgjVRAdpP4BId8giiLrOKmwdAcA0+c7iJnmi2RF1JnSpFM=");
        tokenArr.add("6q3vD8flL4Lf1micXedQK360Rg5pcUUEiFwVv8uX1o4cOofAP/qzEmaAAQiyljiO1ksiDM2IdC59r5PwcSjsEN9NsyKOkjX31kp3KsBK7f22dsu+qg/X84ojg8bg1rtvttbvjbeM73LLvNef3U4t/9w3XuCY5ngMiGmlEVy19g2jr/4RGCBwqmVqCBCdeORXg5iBWYIERJFDyXHphkvZ1kFZA7K3PD6OYpO9yak82wrCfbXg7Ux+pKwY71uyYPottbz+XRmMAUtB2NYd3F8WIkiNJRDVyTFLKY+nTkPrO1zeKejzH+begRc8zMh85GenGg5gbOsOCiEzGEJksljnloErsmayrQWrRBlFNaNr53w74lniCsZ16cpxR933GN0NtIa4vZx7sbjDyLLrV4FB7/Axfc2m5rTLM7C4mSoVti/rRbdv8D75r/iM8PmUaFb4MjPzAOlCOUekaqZ9vhxzeXvaQv7R9SkFF3QQWJqFzcBnxhiOnyNPOl4zqqU1oZTMr0KBRJcH3LB4jV5Qd7W498UaBQNto06Uft/8ZxZqgtOhJVgjVRAdpP4BId8giiLrOKmwdBwfCjy1wkAn1f6gr+I8tzI=");
        tokenArr.add("8Hwkp+hTBDuWHJH570KHuLn6Sq7VifHPmGCnRu+LUVDr/hEDZH5Ak973uo0PhdE7HEK/G4/6PkvwuDS+Q+KxRKuXaCPjVlply3jxg9gfEtPl1efkIpqGPy/Y6dRdrR9UYOOZ5UXlhWHuregd6+7bW+ndEWnbDq3KiU0EOsSgL3/JsQ5flLTVgm5VkZeLeh6cty1LJ2TmGMdL/c56G40pOyugrFytP8w4psfRpEXpimwYHPOR7SgLbkDeP1k8UOL9qzrKc4rrgUo8ICwrt7kpVe6Dx0//5+RzKfLtGj9Vpyd8OOUGJnbVX4rvfqMp4VA5Hg7dvXU/FojJzs6NXVV8jo4jH/U/aXSENFdrxHKIJF+S6Rv1GTxxNUIV5hcVSeEdzjpikrdMvc7Ln+EdKLnXVQ86qPJt+Kdg0+P0wCQXABZaB9dm9K1p6AAg6YwNiaSyT9BzOQAeT2qRdDyzeHdPzg0gwWVjPQTRsEGwq2CXR9RQ7uggyHmf5iRWp9I7oS1hrA+9UwBZ9XvJkCHoQmKNvBzFsYKTeJAYT21QsnqzpJSaKc1X5PNNZuNmC5l0t+XFddtczmOePYAAly4JJdaBgw==");
        tokenArr.add("8Hwkp+hTBDuWHJH570KHuLn6Sq7VifHPmGCnRu+LUVDr/hEDZH5Ak973uo0PhdE7HEK/G4/6PkvwuDS+Q+KxRKuXaCPjVlply3jxg9gfEtN5SqH/oNvailx0Vlq8Ery1Cpoicoo2KzMHFhSZ7fkYs4t2mgGuUm9uhMhnmbcip/0z8yYy1rR8nNpO9gJwGMGZw0N+RWQD8KtuHZ1mr4u+Hk6m1u76PXaeVeKyb0yzaLBJkPrmj6jLifd+A+yeMEyDnHd8dqfIlcgUiM4oFVaJ7f+NZqtGJoUPQUtaW29LUvX5mCglBBCJyBfc3OunKbCNxANxGrAJra77EuRgaaQZb9nIySCG6wQ+MeWhzX8h3pSDgyn7OMKdoZHSNqDXxRFNAElF68DwYZTlQ0Cvybh+U2P3iRbf2U4U/v94TVhn7dp/l+P65ybS7eF0Z+akkB7aaYOgD3E9HB8Cdqb9knzVEWnEUunc9awP/2ltt7ZOREyt5cWD4EoYG/AaWHzI7hMnF4AJhEbxsLt8SYd91NlUlIAvOgaeHuokelUaQZvNfTmCCUiJ8dtdP5hcNrHpblg5iO9FVq7NFcleDDM9ICiv3A==");
        tokenArr.add("8Hwkp+hTBDuWHJH570KHuLn6Sq7VifHPmGCnRu+LUVDr/hEDZH5Ak973uo0PhdE7HEK/G4/6PkvwuDS+Q+KxRC+FXsDSGxhMSPsK70x3W0yqN2b14PsNrePRK9lyHGie+k9f4R6aIfAhlMGdV29KucnZEeMOOWfJadVrLyv4rNPsHbrWO2bVuCIEPrLquRNKdBLaj00UKFb6r7caIfzmfBVqKlL6QL5Wo/vDimX9zSovXKWLXb/3OEd4BYFQrzQy+3ShbT/t6jq79F+Tug+DQL1houUelNS3MiKH+srYwwLUXIdQSjkgeP1rPradiLOsUafcZjlaGp8OZYUMSh4hOBmAEqlPB84SEyXATvvlvsaoFLvgIRLmDTWThY5WhZnn064Dv35BGgZP2q9Y3CXRNI6dRO43gaj5zZuHFpmcoMUvlGos/0C+y9ftB2xw/YNZX/8KZn+zMsl5BlpJcsmAg7ORBt0YpDhYTA25gzMq3VpeMUu/viLQDEgNNCQHd2D4+oe4YrqnTi9CAycbRdPkRo2mVTkO1cB4BUnNwHAexhJ6h0XBqZvMffzXH+7wtlQhF1RAuT1tAIOGPXleaV0ZKQ==");

        for (String token : tokenArr) {
            Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(token)
                    .withTimestamp(date)
                    .execute();
            assertNotNull(resubmitDataCollect);
        }


    }
}
