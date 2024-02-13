package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.BatchCloseType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.terminals.TerminalUtilities;
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
public class VapsBatchTests {
    private String configName = "default";
    private BatchProvider batchProvider;

    public VapsBatchTests() throws ApiException {
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
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
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
    public void test_000_super_manual_batch_close() throws ApiException {
        ManagementBuilder builder = new ManagementBuilder(TransactionType.BatchClose)
                .withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber())
                .withBatchTotals(batchProvider.getTransactionCount(), batchProvider.getTotalDebits(), batchProvider.getTotalCredits())
                .withBatchCloseType(BatchCloseType.EndOfShift);

        Transaction trans = builder.execute("NoBatch");
        assertNotNull(trans);
        assertEquals("000", trans.getResponseCode());
        assertNotNull(trans.getBatchSummary());
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
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));

        Transaction creditAuth = creditAuth(20);
        assertNotNull(creditAuth.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditAuth.getTransactionToken()));

//        Transaction debitSale = debitSale(30);
//        assertNotNull(debitSale.getTransactionToken());
//        assertTrue(TerminalUtilities.checkLRC(debitSale.getTransactionToken()));
//
//        Transaction debitAuth = debitAuth(50);
//        assertNotNull(debitAuth.getTransactionToken());
//        assertTrue(TerminalUtilities.checkLRC(debitAuth.getTransactionToken()));

        Transaction response = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(30),
                BigDecimal.ZERO
        ).execute(configName);
        assertNotNull(response);

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());

        if(summary.getResponseCode().equals("580")) {
            LinkedList<String> tokens = new LinkedList<String>();
            tokens.add(creditSale.getTransactionToken());
            tokens.add(creditAuth.getTransactionToken());
//            tokens.add(debitSale.getTransactionToken());
//            tokens.add(debitAuth.getTransactionToken());

            BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
            assertNotNull(newSummary);
            assertEquals(2, newSummary.getResentTransactions().size());
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
    public void test_241_resubmit_decode_issue() throws ApiException {
        String validToken = "L7TLtcuVGMraLtFUdo7XZrQAGG+zpa3zpGclmTBgwKFeWsFq75nIDPjuFrXU09csdZfPqXnu8TdfUn8qQt5t724qfbASTyaaOXpU6G5Ir+ILZIVODkTYda9g+kpmjWr9rKUqgGJLHK30trozIt9zrON39v1TfX2l+kTlj2R1X4jwFmifAzgb8DYTGGaRpIF62fyIbliX9vNdfaZlSfShqIauQ3WGAuCbN2LNsGL7eFaOTY5azWcultC27QmXMrITX/zYh94Fo/iP0SZQ+lBT5IQCWALmM0uil9AOIPyGsudcLuvR7h6groZ8skno9jlTktVxha1kCtAMxgub2hwIcp04IQ0Fd0ETywVW+Ay0SByT77obP5H4XhACVB+TCzlECCB++SO/QuJ+YRjQ6VNq7PX2eDr3iYduWMPa6WFB0P9ksEdEQywEiczI3M+42sDP/dRKzC+PabCkHYeFb+vYf6EXth8dwi94PM6gQJDALdGwll6MPleDBoX9BcSKI8bqiQcuZF8dTst1SuJ9fkw069IEWb5NY5Ef2U+mOXo/lmcuCbUKbZPImtN+vClmblbB";
        //String invalidToken = "nao1NWxyjJfnI3N5O1KPJxCR/361V9yksMetoPCCkYTjVdqJ3yELrtF4XbYwZwXQ4O/vplu+nXTIjcNkoiFST3bMFMkY/YZ/a2t+nu+2fW8fz2uX84R2ZNSm4qLxGAws9jIvrqrqlw6W5h48h86LOlnG7vB9Do6EbGa8UO4q45mXSHZCPsqLXox0R+iMJoGElsZbu9F2z6V8TcpICIOS/rtnvWeAgAQSdpwYrvVSXlbFrgR4lMeJoZxihpU7l7iej+iALOT2iIOuasCYDFkt2Qp0wOsQOtlAhz8uE6DR23H8AdjXMLVWufhNEtJruso4xV71hYnqAGI8zXXhVgDNdCiVLoRr+qY5L0e57QNRoY+2VpwReDLfVUNzXH3DYztNhv19U1z3lLpsUeSQ4lqxANsK9P2jCpq5yLa524tzGVgJ6eZLENnThFZQo5ILXc+DRkaz6qhV8nFCiTzdnd1hXGz5XUy9WsYstJBzIlpDYusuqSP/nNl9S7xg3uFODKkEcDU6w/VVjPiLko/DgdmbmgQmTMljHgewaJiL2rLcFU9u/634zu0kvKlV8rJLZtiI";
        //String invalidToken = "Qdop97Dsbn7dPfMM63IkEojqEX8SypNlpv4kvFtJOHnNPAX7UWcWKjVQjszE2IVrrEsd+aPJh/LldtPWzIYt+NOgfrj299la6RpvIE51KW4gD6/Jlmayq+0f3u/hnlHpvVqAOOrdXkiVGMk4TuLmxaRPu3xt6v8p+Q0slKJS0dqbh6lMaATmDt/2i0tbTwvhzWEV9e2qmcNFn0Ip/eZRKorwdjxI9+AxVKWSoTl+K7wa2MlSWkj7TLQL+dKv9T86gv/6wthIZNkZr5NwWb5gisYtD4rfeXbu+bd0DjLJKuPCHi7R7fVIBYDBLcBlghxKuYuOTAH8fcGGLsLVslGKZ0qD5WHEcUHVRtggCsynCP6qkoNcD0jIZJZSHl/xsiW+YQ0Q/N0ysDl7puLnLdZDlkoo4qsYZmBHuGeS8R9HvOmWlYbYzv/Nc4oki+7cfll3N2yn+sIeLvD0mvcb0Hiq/Pr4taaUIHgpAdcZNyeqPP2BKiNAFpepmM4Psvw6kv/LOQ1ywl6M+f9WcDjIPWKxRyZI6ObWHiTbX19jswMddtl9mdEkGDZ0id9o8UCbLxUPvPmYzV0oH8XG84SjxeBnffHqPObTynm6Av18v70+GJnmhJzrBixAZc0KuiE1O9mj39DRFESaOsBAuNARCIZIw==";

        LinkedList<String> tokens = new LinkedList<String>();
        tokens.add(validToken);
        //tokens.add(invalidToken);

        BatchSummary mockSummary = new BatchSummary();
        mockSummary.setResponseCode("580");
        mockSummary.resubmitTransactions(tokens);
    }

    // Negative scenario - refund with 909 error
    @Test
    public void test_002_resubmits_refund_10258_Issue() throws ApiException {
        configName = "NoBatch";

        Transaction creditSaleResponse = creditSale(9.99);
        assertNotNull(creditSaleResponse.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSaleResponse.getTransactionToken()));

        CreditTrackData track = TestCards.VisaSwipe();
        Transaction refundResponse = track.refund(new BigDecimal(9.99))
                .withOfflineAuthCode(creditSaleResponse.getAuthorizationCode())
                .withCurrency("USD")
                .execute();
        assertNotNull(refundResponse);

        Transaction response = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(30),
                BigDecimal.ZERO
        ).execute(configName);
        assertNotNull(response);

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());

        LinkedList<String> tokens = new LinkedList<String>();
        tokens.add(creditSaleResponse.getTransactionToken());
        tokens.add(refundResponse.getTransactionToken());
        BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
        assertNotNull(newSummary);
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
    @Test
    public void test_currency_code_coverage() throws ApiException {
        configName = "default";

        creditSale(10);
        creditAuth(20);


        Transaction response = BatchService.closeBatch(1, new BigDecimal(30), new BigDecimal(0))
                .withCurrency("CAD")
                .execute(configName);
        assertNotNull(response);
        assertNotNull(response.getBatchSummary());
    }
    private Transaction creditSale(double amount) throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        AuthorizationBuilder builder = track.charge(new BigDecimal(amount))
                .withAllowPartialAuth(true)
                .withCurrency("USD");

        if(configName.equals("NoBatch")) {
            builder.withBatchNumber(batchProvider.getBatchNumber(), batchProvider.getSequenceNumber());
        }

        Transaction response = builder.execute(configName);
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
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

    @Test
    public void test_10289_partialAmount_retransmitDataCollect_withBatchSummary() throws ApiException {
        configName = "NoBatch";

        Transaction creditSale = creditSale(11.51);
        assertNotNull(creditSale.getTransactionToken());
        assertTrue(TerminalUtilities.checkLRC(creditSale.getTransactionToken()));

        Transaction response = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(11.51),
                BigDecimal.ZERO
        ).execute(configName);
        assertNotNull(response);

        BatchSummary summary = response.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());

        if(summary.getResponseCode().equals("580")) {
            LinkedList<String> tokens = new LinkedList<String>();
            tokens.add(creditSale.getTransactionToken());

            BatchSummary newSummary = summary.resubmitTransactions(tokens, configName);
            assertNotNull(newSummary);
        }
        else {
            assertTrue(summary.getResponseCode().equals("500") || summary.getResponseCode().equals("501"));
        }
    }

    @Test
    public void test10289_044_retransmit_data_collect_partialAmount_withNetworkService() throws ApiException {
        configName = "NoBatch";
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction dataCollect = track.charge(new BigDecimal(11.51))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(dataCollect);
        assertEquals(dataCollect.getResponseMessage(), "002", dataCollect.getResponseCode());
        assertNotNull(dataCollect.getTransactionToken());


        Transaction batchClose = BatchService.closeBatch(
                batchProvider.getBatchNumber(),
                batchProvider.getSequenceNumber(),
                new BigDecimal(11.51),
                BigDecimal.ZERO
        ).execute(configName);
        assertNotNull(batchClose);


        BatchSummary summary = batchClose.getBatchSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTransactionToken());

        Transaction resubmitDataCollect = NetworkService.resubmitDataCollect(dataCollect.getTransactionToken())
                .withForceToHost(true)
                .execute();

        assertNotNull(resubmitDataCollect);
        assertEquals(resubmitDataCollect.getResponseMessage(), "000", resubmitDataCollect.getResponseCode());

        Transaction resubmitBatch = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .withForceToHost(true)
                .execute();

        assertNotNull(resubmitBatch);
        assertEquals(resubmitBatch.getResponseMessage(), "580", resubmitBatch.getResponseCode());


    }
}
