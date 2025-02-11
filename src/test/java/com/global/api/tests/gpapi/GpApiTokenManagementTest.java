package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.StringUtils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;

public class GpApiTokenManagementTest extends BaseGpApiTest {

    private static CreditCardData card;
    private static String token;
    private final BigDecimal amount = new BigDecimal("15.25");
    private final String currency = "USD";

    public GpApiTokenManagementTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        try {
            token = card.tokenize();

            assertFalse(StringUtils.isNullOrEmpty(token), "Token could not be generated.");
        } catch (GatewayException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void TokenizePaymentMethod() throws ApiException {
        card.setExpYear(expYear);

        String response =
                card
                        .tokenize();

        assertNotNull(response);
        assertTrue(response.startsWith("PMT_"));
    }

    @Test
    public void TokenizePaymentMethodSingle() throws ApiException {
        card.setExpYear(expYear);

        String[] permissions = new String[]{"PMT_POST_Create_Single"};
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setPermissions(permissions);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "singleUseToken");

        Transaction response =
                card
                        .tokenize(true, PaymentMethodUsageMode.SINGLE)
                        .execute("singleUseToken");

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("PMT_"));
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());
        assertEquals(PaymentMethodUsageMode.SINGLE, response.getTokenUsageMode());
    }

    @Test
    public void TokenizePaymentMethodMultiple() throws ApiException {
        card.setExpYear(expYear);

        Transaction response =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertTrue(response.getToken().startsWith("PMT_"));
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response.getTokenUsageMode());
    }

    @Test
    public void VerifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertEquals("XXXXXXXXXXXX1111", response.getCardLast4());
        assertEquals("VISA", response.getCardType());
    }

    @Test
    public void VerifyTokenizedPaymentMethod_withIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertEquals("XXXXXXXXXXXX1111", response.getCardLast4());
        assertEquals("VISA", response.getCardType());

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + response.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void UpdateTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030); // Expiration Year is updated until 2030

        assertTrue(tokenizedCard.updateTokenExpiry());

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        // assertEquals(30, response.getCardExpYear());

        tokenizedCard.setExpYear(2025);
        assertTrue(tokenizedCard.updateTokenExpiry());
    }

    @Test
    public void UpdateTokenizedPaymentMethodWrongId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40116", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSaleWithTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response = tokenizedCard
                .charge(amount)
                .withCurrency(currency)
                .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditSaleWithTokenizedPaymentMethod_WithStoredCredentials() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        StoredCredential storedCredential = new StoredCredential();

        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response = tokenizedCard
                .charge(amount)
                .withCurrency(currency)
                .withStoredCredential(storedCredential)
                .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();

        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response = card
                .charge(amount)
                .withCurrency(currency)
                .withStoredCredential(storedCredential)
                .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Disabled
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteToken() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken());
        assertFalse(tokenizedCard.deleteToken());

        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Disabled
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteToken_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken());
        assertFalse(tokenizedCard.deleteToken());

        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Test
    public void CardTokenizationThenPayingWithToken_SingleToMultiUse() throws ApiException {
        String[] permissions = new String[]{"PMT_POST_Create_Single"};
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setPermissions(permissions);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "singleUseToken");

        // process an auto-capture authorization
        String tokenId =
                card
                        .tokenize(true, "singleUseToken", PaymentMethodUsageMode.SINGLE);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("James Mason");

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertTrue(response.getToken().startsWith("PMT_"));

        tokenizedCard.setToken(response.getToken());

        response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

}
