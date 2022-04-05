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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiTokenManagementTests extends BaseGpApiTest {

    private static CreditCardData card;
    private static String token;
    private final BigDecimal amount = new BigDecimal("15.25");
    private final String currency = "USD";

    public GpApiTokenManagementTests() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        try {
            token = card.tokenize(GP_API_CONFIG_NAME);

            assertFalse("Token could not be generated.", StringUtils.isNullOrEmpty(token));
        } catch (GatewayException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void TokenizePaymentMethod() throws ApiException {
        card.setExpYear(expYear);

        String response =
                card
                        .tokenize(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertTrue(response.startsWith("PMT_"));
    }

    @Test
    public void VerifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

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
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
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

        assertTrue(tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME));

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        // assertEquals(30, response.getCardExpYear());

        tokenizedCard.setExpYear(2025);
        assertTrue(tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME));
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
                    .execute(GP_API_CONFIG_NAME);
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
                .execute(GP_API_CONFIG_NAME);

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
                .execute(GP_API_CONFIG_NAME);

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
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Ignore
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteToken() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Ignore
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteToken_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Test
    public void CardTokenizationThenPayingWithToken_SingleToMultiUse() throws ApiException {
        // process an auto-capture authorization
        String tokenId =
                card
                        .tokenize(true, GP_API_CONFIG_NAME, PaymentMethodUsageMode.Single);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("James Mason");

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertTrue(response.getToken().startsWith("PMT_"));

        tokenizedCard.setToken(response.getToken());

        response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

}