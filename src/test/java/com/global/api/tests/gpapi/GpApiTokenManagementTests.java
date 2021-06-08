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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiTokenManagementTests extends BaseGpApiTest {

    private static CreditCardData card;
    private static String token;

    public GpApiTokenManagementTests() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg")
                .setAppKey("ockJr6pv6KFoGiZA");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        try {
            token = card.tokenize(GP_API_CONFIG_NAME);

            assertTrue("Token could not be generated.", !StringUtils.isNullOrEmpty(token));
        }
        catch (GatewayException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void a1_TokenizePaymentMethod() throws ApiException {
        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("4111111111111111");
        cardData.setExpMonth(12);
        cardData.setExpYear(2021);

        String response =
                cardData
                        .tokenize(GP_API_CONFIG_NAME);

        assertNotNull(response);
    }

    @Test
    public void b1_VerifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
        assertEquals("XXXXXXXXXXXX1111", response.getCardLast4());
        assertEquals("VISA", response.getCardType());
    }

    @Test
    public void b2_VerifyTokenizedPaymentMethod_withIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
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
    public void d1_UpdateTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030); // Expiration Year is updated until 2030

        assertTrue(tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME));

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
        // assertEquals(30, response.getCardExpYear());

        tokenizedCard.setExpYear(2025);
        assertTrue(tokenizedCard.updateTokenExpiry(GP_API_CONFIG_NAME));
    }

    @Test
    public void d3_UpdateTokenizedPaymentMethodWrongId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID().toString());

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("40116", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    public void e1_CreditSaleWithTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response = tokenizedCard
                .charge(new BigDecimal("19.99"))
                .withCurrency("USD")
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void e2_CreditSaleWithTokenizedPaymentMethod_WithStoredCredentials() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        StoredCredential storedCredential = new StoredCredential();

        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response = tokenizedCard
                .charge(new BigDecimal("15.25"))
                .withCurrency("USD")
                .withStoredCredential(storedCredential)
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void e3_CreditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();

        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response = card
                .charge(new BigDecimal("15.25"))
                .withCurrency("USD")
                .withStoredCredential(storedCredential)
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void f1_DeleteToken() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Test
    public void f2_DeleteToken_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));
        assertFalse(tokenizedCard.deleteToken(GP_API_CONFIG_NAME));

        try {
            tokenizedCard
                    .verify()
                    .withCurrency("USD")
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Test
    public void g1_cardTokenizationThenPayingWithToken_SingleToMultiUse() throws ApiException {
        // process an auto-capture authorization
        String tokenId =
                card
                        .tokenize(true, GP_API_CONFIG_NAME, PaymentMethodUsageMode.Single);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("James Mason");

        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("10"))
                        .withCurrency("USD")
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertTrue(response.getToken().startsWith("PMT_"));

        tokenizedCard.setToken(response.getToken());

        response =
                tokenizedCard
                        .charge(new BigDecimal("10"))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

}