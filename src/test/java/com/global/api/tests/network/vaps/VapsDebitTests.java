package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
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
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

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

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
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
        acceptorConfig.setPinlessDebit(true);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

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
    public void test_149_pre_authorization() throws ApiException {
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

        if(!completion.getResponseCode().equals("000")) {
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

    @Test @Ignore
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

    @Test @Ignore
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

    @Test @Ignore
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

        Transaction response = track.authorize(new BigDecimal(40),true)
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
    public void test_177_reverse_sale_cashBack_with_fee() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.Surcharge,new BigDecimal(5))
                .withCashBack(new BigDecimal(3))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .withCashBackAmount(new BigDecimal(3))
                .execute();
        assertNotNull(reversal);
        assertEquals("400",reversal.getResponseCode());
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
//                .withSystemTraceAuditNumber(stan.generateStan())
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

        assertEquals("000",response.getResponseCode());
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

}
