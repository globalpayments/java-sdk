package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiTokenManagementErrorTest extends BaseGpApiTest {

    private static final String currency = "USD";

    public GpApiTokenManagementErrorTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        String token = card.tokenize();

        assertNotNull("Token could not be generated.", token);
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithMalformedId() throws ApiException {
        String token = "This_is_not_a_payment_id";

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        boolean exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - payment_method.id: " + token + " contains unexpected data", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithMissingCardNumber() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        boolean exceptionCaught = false;
        try {
            card
                    .tokenize();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields : number", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void VerifyTokenizedPaymentMethod_WithRandomId() throws ApiException {
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
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void UpdateTokenizedPaymentMethod_WithMalformedId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("This_is_not_a_payment_id");
        tokenizedCard.setExpMonth(expMonth);
        tokenizedCard.setExpYear(expYear);

        boolean exceptionCaught = false;
        try {
            tokenizedCard.updateTokenExpiry();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 400 - payment_method.id: This_is_not_a_payment_id contains unexpected data"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void UpdateTokenizedPaymentMethod_WithRandomId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());
        tokenizedCard.setExpMonth(expMonth);
        tokenizedCard.setExpYear(expYear);

        boolean exceptionCaught = false;
        try {
            tokenizedCard.updateTokenExpiry();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 404 - payment_method"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Ignore
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithNonExistingId() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());
        assertTrue(tokenizedCard.deleteToken());

        boolean exceptionCaught = false;
        try {
            tokenizedCard.deleteToken();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }

        exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Ignore
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithRandomId() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;
        try {
            tokenizedCard.deleteToken();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 404 - payment_method"));
        } finally {
            assertTrue(exceptionCaught);
        }

        exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40116", ex.getResponseText());
            assertEquals("Status Code: 404 - payment_method " + tokenizedCard.getToken() + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Ignore
    // Credentials on this test have no permissions to delete a tokenized card
    // Test passed using secret credentials with permissions to delete a tokenized card
    @Test
    public void DeleteTokenizedPaymentMethod_WithMalformedId() throws ApiException {
        String token = "This_is_not_a_payment_id";

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        boolean exceptionCaught = false;
        try {
            tokenizedCard.deleteToken();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - payment_method.id: This_is_not_a_payment_id contains unexpected data", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }

        exceptionCaught = false;
        try {
            tokenizedCard
                    .verify()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - payment_method.id: This_is_not_a_payment_id contains unexpected data", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

}