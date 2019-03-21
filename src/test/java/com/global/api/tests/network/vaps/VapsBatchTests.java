package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class VapsBatchTests {
    private String configName = "default";
    private BatchProvider batchProvider;

    public VapsBatchTests() throws ApiException {
        Address address = new Address();
        address.setName("7-ELEVEN");
        address.setStreetAddress1("8002 SOUTH STATE");
        address.setCity("MIDVALE");
        address.setPostalCode("840473293");
        address.setState("UT");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
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

        batchProvider = BatchProvider.getInstance();

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setSecondaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setPrimaryEndpoint("test.7eleven.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0017");
        config.setTerminalId("0000123456701");
        config.setUniqueDeviceId("0001");
        config.setMerchantType("5541");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(batchProvider);

        ServicesContainer.configureService(config);

        config.setBatchProvider(null);
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

        BatchSummary summary = BatchService.closeBatch(1, new BigDecimal(30), new BigDecimal(80));
        assertNotNull(summary);
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

        BatchSummary summary = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(30),
                new BigDecimal(80),
                configName
        );
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

        BatchSummary response = BatchService.closeBatch(BatchCloseType.EndOfShift);
        assertNotNull(response);
        assertTrue(response.isBalanced());
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
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

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
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

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
}
