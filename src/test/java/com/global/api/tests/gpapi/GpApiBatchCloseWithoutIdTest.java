package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.BatchService;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for closing a GP API batch without specifying a batch ID.
 * Covers multi-merchant and standalone merchant scenarios using both default and named
 * GP API configurations, with optional scoping by currency and payment method type.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GpApiBatchCloseWithoutIdTest extends BaseGpApiTest {
    private CreditCardData card;

    private static final String CURRENCY = "GBP";
    private static final BigDecimal AMOUNT = new BigDecimal("13.00");

    // Account-to-app mapping is environment-bound; keep values stable and label by scenario usage.
    private static final String NAMED_CONFIG_ACCOUNT = "test_account_CungkardAk";
    private static final String DEFAULT_INLINE_CONFIG_ACCOUNT = "test_account_QxiWq2X9sg";

    private static final String MULTI_MERCHANT_APP_ID = "wuqol13QQJu0vcjbdN9892cl5IcyJgPU";
    private static final String MULTI_MERCHANT_APP_KEY = "jdOJNxsGbE0nLeTx";
    private static final String MULTI_MERCHANT_TARGET_MERCHANT_ID = "MER_3abc0724f49c40e59a61309ae6d37dfd";

    private static final String STANDALONE_MERCHANT_INLINE_APP_ID = "UDVB5ngQEEn6wPLA9gJZMj25Uw6B9wXcXbd9XTu7tEq9pbUg";
    private static final String STANDALONE_MERCHANT_INLINE_APP_KEY = "UICgLDNtzLXK7L2p8AGcRfaf9PzvYPCuVGJxQoWjqUCr2BERs80xNBuSDe9uB9G7";

    private static final String STANDALONE_MERCHANT_NAMED_APP_ID = "naL0LWpbHEHJSw9aSmap1u3W1rqusAA4587WzEBikVG4zbEK";
    private static final String STANDALONE_MERCHANT_NAMED_APP_KEY = "LeC5h0BuYfVnvTe3b0732AHG2HGtxptNNq9RfoQcnP0sozfKkYyLvNtiQf5md3BB";

    private static final String MULTI_MERCHANT_NAMED_CONFIG = "MultiMerchantNamedConfig";
    private static final String STANDALONE_MERCHANT_NAMED_CONFIG = "StandaloneMerchantNamedConfig";

    /**
     * Sets up GP API configurations for multi-merchant and standalone merchant scenarios,
     * and initialises a card-present credit card for use in each test.
     */
    public GpApiBatchCloseWithoutIdTest() throws ConfigurationException {
        GpApiConfig multiMerchantConfig = gpApiSetup(MULTI_MERCHANT_APP_ID, MULTI_MERCHANT_APP_KEY, Channel.CardPresent);
        multiMerchantConfig.setMerchantId(MULTI_MERCHANT_TARGET_MERCHANT_ID);
        multiMerchantConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName(NAMED_CONFIG_ACCOUNT);
        multiMerchantConfig.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(multiMerchantConfig);
        ServicesContainer.configureService(multiMerchantConfig, MULTI_MERCHANT_NAMED_CONFIG);

        GpApiConfig standaloneNamedConfig = new GpApiConfig();
        standaloneNamedConfig.setAppId(STANDALONE_MERCHANT_NAMED_APP_ID);
        standaloneNamedConfig.setAppKey(STANDALONE_MERCHANT_NAMED_APP_KEY);
        standaloneNamedConfig.setChannel(Channel.CardPresent);
        standaloneNamedConfig.setRequestLogger(new RequestConsoleLogger());
        standaloneNamedConfig.setEnableLogging(true);
        standaloneNamedConfig.setCountry("US");
        standaloneNamedConfig.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountName(NAMED_CONFIG_ACCOUNT));

        ServicesContainer.configureService(standaloneNamedConfig, STANDALONE_MERCHANT_NAMED_CONFIG);

        card = new CreditCardData();
        card.setNumber("5425230000004415");
        card.setExpMonth(12);
        card.setExpYear(2026);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    /**
     * Multi-merchant default config (inline): close batch without a batch ID using account name only,
     * configured inline with a multi-merchant GP API setup.
     */
    @Test
    @Order(1)
    void CloseBatch_WithoutBatchId_MultiMerchant_DefaultConfig_AccountOnly() throws Exception {
        GpApiConfig multiMerchantConfig = new GpApiConfig();
        multiMerchantConfig.setAppId(MULTI_MERCHANT_APP_ID);
        multiMerchantConfig.setAppKey(MULTI_MERCHANT_APP_KEY);
        multiMerchantConfig.setChannel(Channel.CardPresent);
        multiMerchantConfig.setRequestLogger(new RequestConsoleLogger());
        multiMerchantConfig.setEnableLogging(true);
        multiMerchantConfig.setMerchantId(MULTI_MERCHANT_TARGET_MERCHANT_ID);
        multiMerchantConfig.setCountry("US");
        multiMerchantConfig.setAccessTokenInfo(new AccessTokenInfo().setTransactionProcessingAccountName(DEFAULT_INLINE_CONFIG_ACCOUNT));

        ServicesContainer.configureService(multiMerchantConfig);


        Transaction transaction = card.charge(AMOUNT).withCurrency("USD").execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch();
        assertBatchCloseResponse(batchSummary, AMOUNT);
    }

    /**
     * Multi-merchant named config: close batch without a batch ID, scoped by currency and payment methods,
     * routed through a named GP API configuration for a multi-merchant setup.
     */
    @Test
    @Order(2)
    void CloseBatch_WithoutBatchId_MultiMerchant_NamedConfig_WithCurrencyAndPaymentMethods() throws Exception {
        Transaction transaction = card.charge(AMOUNT).withCurrency(CURRENCY).execute(MULTI_MERCHANT_NAMED_CONFIG);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(CURRENCY, new PaymentMethodName[]{PaymentMethodName.Card}, MULTI_MERCHANT_NAMED_CONFIG);
        assertBatchCloseResponse(batchSummary, AMOUNT);
    }

    /**
     * Multi-merchant default config: close batch without a batch ID, scoped by currency and payment methods,
     * using the default GP API configuration for a multi-merchant setup.
     */
    @Test
    @Order(3)
    void CloseBatch_WithoutBatchId_MultiMerchant_DefaultConfig_WithCurrencyAndPaymentMethods() throws Exception {
        Transaction transaction = card.charge(AMOUNT).withCurrency(CURRENCY).execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(CURRENCY, new PaymentMethodName[]{PaymentMethodName.Card});
        assertBatchCloseResponse(batchSummary, AMOUNT);
    }

    /**
     * Verifies that closing a batch without a batch ID using an invalid currency throws a {@link GatewayException}.
     */
    @Test
    @Order(4)
    void CloseBatch_WithoutBatchId_InvalidCurrency_ThrowsGatewayException() {
        GatewayException ex = assertThrows(GatewayException.class, () ->
                BatchService.closeBatch("PLN", new PaymentMethodName[]{PaymentMethodName.Card}, MULTI_MERCHANT_NAMED_CONFIG)
        );
        assertEquals("CONFIGURATION_DOES_NOT_EXIST", ex.getResponseCode());
        assertEquals("40041", ex.getResponseText());
        assertTrue(ex.getMessage().contains("Merchant configuration does not exist"));
    }

    /**
     * Multi-merchant default config: gateway returns CONFIGURATION_DOES_NOT_EXIST (40041) when
     * the close-batch currency does not match the original transaction currency.
     */
    @Test
    @Order(5)
    void CloseBatch_WithoutBatchId_MultiMerchant_DefaultConfig_CurrencyMismatch_ThrowsGatewayException() throws Exception {
        Transaction transaction = card.charge(AMOUNT).withCurrency(CURRENCY).execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        GatewayException ex = assertThrows(GatewayException.class, () ->
                BatchService.closeBatch("USD", new PaymentMethodName[]{PaymentMethodName.Card})
        );
        assertEquals("CONFIGURATION_DOES_NOT_EXIST", ex.getResponseCode());
        assertEquals("40041", ex.getResponseText());
        assertTrue(ex.getMessage().contains("Merchant configuration does not exist"));
    }

    /**
     * Multi-merchant named config: gateway returns CONFIGURATION_DOES_NOT_EXIST (40041) when
     * the close-batch currency does not match the original transaction currency.
     */
    @Test
    @Order(6)
    void CloseBatch_WithoutBatchId_MultiMerchant_NamedConfig_CurrencyMismatch_ThrowsGatewayException() throws Exception {
        Transaction transaction = card.charge(AMOUNT).withCurrency(CURRENCY).execute(MULTI_MERCHANT_NAMED_CONFIG);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        GatewayException ex = assertThrows(GatewayException.class, () ->
                BatchService.closeBatch("USD", new PaymentMethodName[]{PaymentMethodName.Card}, MULTI_MERCHANT_NAMED_CONFIG)
        );
        assertEquals("CONFIGURATION_DOES_NOT_EXIST", ex.getResponseCode());
        assertEquals("40041", ex.getResponseText());
        assertTrue(ex.getMessage().contains("Merchant configuration does not exist"));
    }

    /**
     * Standalone merchant default config (inline): close batch without a batch ID using account name only,
     * configured inline with a standalone merchant GP API setup.
     */
    @Test
    @Order(7)
    void CloseBatch_WithoutBatchId_StandaloneMerchant_DefaultConfig_AccountOnly() throws Exception {
        GpApiConfig standaloneInlineConfig = new GpApiConfig();
        standaloneInlineConfig.setAppId(STANDALONE_MERCHANT_INLINE_APP_ID);
        standaloneInlineConfig.setAppKey(STANDALONE_MERCHANT_INLINE_APP_KEY);
        standaloneInlineConfig.setChannel(Channel.CardPresent);
        standaloneInlineConfig.setRequestLogger(new RequestConsoleLogger());
        standaloneInlineConfig.setEnableLogging(true);
        standaloneInlineConfig.setCountry("US");
        standaloneInlineConfig.setAccessTokenInfo(
                new AccessTokenInfo().setTransactionProcessingAccountName(DEFAULT_INLINE_CONFIG_ACCOUNT)
        );

        ServicesContainer.configureService(standaloneInlineConfig);

        Transaction transaction = card.charge(AMOUNT).withCurrency("USD").execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch();
        assertBatchCloseResponse(batchSummary, AMOUNT);
    }

    /**
     * Standalone merchant named config \(no MerchantId\): close batch without a batch ID, scoped by
     * currency and payment methods, routed through a named GP API configuration.
     */
    @Test
    @Order(8)
    void CloseBatch_WithoutBatchId_StandaloneMerchant_NamedConfig_WithCurrencyAndPaymentMethods() throws Exception {
        Transaction transaction = card.charge(AMOUNT).withCurrency(CURRENCY).execute(STANDALONE_MERCHANT_NAMED_CONFIG);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        waitForGpApiReplication();

        BatchSummary batchSummary = BatchService.closeBatch(
                CURRENCY,
                new PaymentMethodName[]{PaymentMethodName.Card},
                STANDALONE_MERCHANT_NAMED_CONFIG
        );
        assertBatchCloseResponse(batchSummary, AMOUNT);
    }

    /**
     * Asserts that a batch close response is valid: the summary is non-null, the status is {@code CLOSED},
     * the transaction count is at least 1, and the total amount is greater than or equal to the expected amount.
     *
     * @param batchSummary the {@link BatchSummary} returned from closing the batch
     * @param amount       the minimum expected total amount for the batch
     */
    private static void assertBatchCloseResponse(BatchSummary batchSummary, BigDecimal amount) {
        assertNotNull(batchSummary);
        assertEquals(CLOSED, batchSummary.getStatus());
        assertTrue(batchSummary.getTransactionCount() >= 1);
        assertTrue(batchSummary.getTotalAmount().compareTo(amount) >= 0);
    }

    /**
     * Asserts that a transaction response is valid: the transaction is non-null, the response code is
     * {@code SUCCESS}, and the response message matches the expected transaction status.
     *
     * @param transaction    the {@link Transaction} to validate
     * @param expectedStatus the expected {@link TransactionStatus}
     */
    private void assertTransactionResponse(Transaction transaction, TransactionStatus expectedStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(expectedStatus.getValue(), transaction.getResponseMessage());
    }
}
