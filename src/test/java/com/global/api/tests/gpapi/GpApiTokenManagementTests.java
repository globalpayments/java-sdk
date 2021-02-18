package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import lombok.SneakyThrows;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiTokenManagementTests {

    private static CreditCardData card;
    private static String token;

    @BeforeClass
    @SneakyThrows
    public static void a_Initialize() {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh");

//      With these Nacho credentials. DeleteToken works
//        config
//                .setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll")
//                .setAppKey("QDsW1ETQKHX6Y4TA");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "GpApiConfig");

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        token = card.tokenize("GpApiConfig");

        assertNotNull("Token could not be generated.", token);
    }

    @Test
    public void b1_verifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertEquals("00", response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());
        assertEquals("xxxxxxxxxxxx1111", response.getCardLast4());
        assertEquals(25, response.getCardExpYear());
        assertEquals(12, response.getCardExpMonth());
        assertEquals("VISA", response.getCardType());
    }

    @Test
    public void b2_verifyTokenizedPaymentMethod_withIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withIdempotencyKey(idempotencyKey)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());
        assertEquals("xxxxxxxxxxxx1111", response.getCardLast4());
        assertEquals(25, response.getCardExpYear());
        assertEquals(12, response.getCardExpMonth());
        assertEquals("VISA", response.getCardType());

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withIdempotencyKey(idempotencyKey)
                    .execute("GpApiConfig");
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
    public void b3_verifyTokenizedPaymentMethodWrongId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID().toString());

        try {
            tokenizedCard
                    .verify()
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - PAYMENT_METHODS " + tokenizedCard.getToken() +
                    " not found at this /ucp/payment-methods/" + tokenizedCard.getToken(), ex.getMessage());
        }
    }

    @Test
    public void c1_detokenizePaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        CreditCardData detokenizedCard = tokenizedCard.detokenize("GpApiConfig");

        assertNotNull(detokenizedCard);
        assertEquals(card.getNumber(), detokenizedCard.getNumber());
        assertEquals(card.getExpMonth(), detokenizedCard.getExpMonth());
        assertEquals(card.getExpYear(), detokenizedCard.getExpYear());
        assertEquals(card.getShortExpiry(), detokenizedCard.getShortExpiry());
    }

    @Test
    public void c2_detokenizePaymentMethod_withIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        CreditCardData detokenizedCard =
                tokenizedCard
                        .detokenizeWithIdemPotencyKey("GpApiConfig", idempotencyKey);

        assertNotNull(detokenizedCard);
        assertEquals(card.getNumber(), detokenizedCard.getNumber());
        assertEquals(card.getExpMonth(), detokenizedCard.getExpMonth());
        assertEquals(card.getExpYear(), detokenizedCard.getExpYear());
        assertEquals(card.getShortExpiry(), detokenizedCard.getShortExpiry());

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .detokenizeWithIdemPotencyKey("GpApiConfig", idempotencyKey);
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
    public void c3_detokenizePaymentMethodWrongId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID().toString());

        try {
            tokenizedCard
                    .detokenize("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("50028", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 404 - payment_method.id: " + tokenizedCard.getToken() + " not found", ex.getMessage());
        }
    }

    @Test
    public void d1_updateTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030); // Expiration Year is updated until 2030

        assertTrue(tokenizedCard.updateTokenExpiry("GpApiConfig"));

        Transaction response =
                tokenizedCard
                        .verify()
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());
        assertEquals(30, response.getCardExpYear());

        tokenizedCard.setExpYear(2025);
        assertTrue(tokenizedCard.updateTokenExpiry("GpApiConfig"));
    }

    @Test
    public void d2_updateTokenizedPaymentMethod_withIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2030); // Expiration Year is updated until 2030

        assertTrue(tokenizedCard.updateTokenExpiryWithIdemPotencyKey("GpApiConfig", idempotencyKey));
        assertFalse(tokenizedCard.updateTokenExpiryWithIdemPotencyKey("GpApiConfig", idempotencyKey));

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .updateTokenExpiryWithIdemPotencyKey("GpApiConfig", idempotencyKey);
        } catch (Exception ex) {
            exceptionCaught = true;
            assertEquals("40031", ex.getLocalizedMessage());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
        } finally {
            assertTrue(exceptionCaught);
        }

        Transaction response =
                tokenizedCard
                        .verify()
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("ACTIVE", response.getResponseMessage());

        tokenizedCard.setExpYear(2025);
        assertTrue(tokenizedCard.updateTokenExpiry("GpApiConfig"));
    }

    @Test
    public void d3_updateTokenizedPaymentMethodWrongId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID().toString());

        try {
            tokenizedCard
                    .verify()
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - PAYMENT_METHODS " + tokenizedCard.getToken() +
                    " not found at this /ucp/payment-methods/" + tokenizedCard.getToken(), ex.getMessage());
        }
    }

    @Test
    public void e1_creditSaleWithTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response = tokenizedCard
                .charge(new BigDecimal("19.99"))
                .withCurrency("USD")
                .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void e2_creditSaleWithTokenizedPaymentMethod_WithStoredCredentials() throws ApiException {
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
                .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void e3_creditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();

        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response = card
                .charge(new BigDecimal("15.25"))
                .withCurrency("USD")
                .withStoredCredential(storedCredential)
                .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void f1_tokenizePaymentMethod() {
        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("4111111111111111");
        cardData.setExpMonth(12);
        cardData.setExpYear(2021);

        String response = cardData
                .tokenize("GpApiConfig");

        assertNotNull(response);
    }

    @Test
    public void f2_tokenizePaymentMethod_WithIdempotencyKey() {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("4111111111111111");
        cardData.setExpMonth(12);
        cardData.setExpYear(2021);

        String response =
                cardData
                        .tokenizeWithIdempotencyKey(true, "GpApiConfig", idempotencyKey);

        assertNotNull(response);

        boolean exceptionCaught = false;
        try {
            cardData
                    .tokenizeWithIdempotencyKey(true, "GpApiConfig", idempotencyKey);
        } catch (Exception ex) {
            exceptionCaught = true;
//            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getLocalizedMessage());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void f3_tokenizePaymentMethodMissingCardNumber() {
        CreditCardData cardData = new CreditCardData();

        try {
            cardData
                    .tokenize("GpApiConfig");

        } catch (Exception ex) {
            assertEquals("Error occurred while communicating with gateway.", ex.getMessage());
        }
    }

    @Ignore
    // Used account/app does not have permissions to delete a token
    // In: https://developer.globalpay.com/developer-apps it has not got a check on item: [POST] Delete
    @Test
    public void g1_deleteToken() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteToken("GpApiConfig"));

        try {
            tokenizedCard
                    .verify()
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Ignore
    // Used account/app does not have permissions to delete a token
    // In: https://developer.globalpay.com/developer-apps it has not got a check on item: [POST] Delete
    @Test
    public void g2_deleteToken_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        assertTrue(tokenizedCard.deleteTokenWithIdempotencyKey("GpApiConfig", idempotencyKey));

        boolean exceptionCaught = false;
        try {
            tokenizedCard.deleteTokenWithIdempotencyKey("GpApiConfig", idempotencyKey);
        } catch (ApiException ex) {
            exceptionCaught = true;
//            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getLocalizedMessage());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
        } finally {
            assertTrue(exceptionCaught);
        }

        try {
            tokenizedCard
                    .verify()
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
        }
    }

    @Test
    public void g3_deleteToken_WrongToken() {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(UUID.randomUUID().toString());

        try {
            tokenizedCard
                    .deleteToken("GpApiConfig");
        } catch (ApiException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getMessage());
        }
    }

}