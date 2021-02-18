package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.enums.Channel;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiCreditTests {

    static final String SUCCESS = "SUCCESS";
    static final String VERIFIED = "VERIFIED";

    private final CreditCardData card;

    public GpApiCreditTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "GpApiConfig");

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(5);
        card.setExpYear(2025);
        card.setCvn("852");
    }

    @Test
    public void creditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void creditAuthorizationAndCapture() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal("16"))
                        .withGratuity(new BigDecimal("2"))
                        .execute("GpApiConfig");

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void creditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(new BigDecimal("16"))
                    .withIdempotencyKey(idempotencyKey)
                    .withGratuity(new BigDecimal("2"))
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
    public void creditAuthorization_CP() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        ServicesContainer.configureService(config, "GpApiConfig");

        CreditTrackData creditTrackData = new CreditTrackData();
        creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                creditTrackData
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void creditAuthorizationAndCapture_CP() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        ServicesContainer.configureService(config, "GpApiConfig");

        CreditTrackData creditTrackData = new CreditTrackData();
        creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                creditTrackData
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal(14))
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void creditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId(UUID.randomUUID().toString());

        try {
            authorization
                    .capture(new BigDecimal(16))
                    .withGratuity(new BigDecimal(2))
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

    @Test
    public void creditSale() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");

        Transaction response =
                card
                        .charge(new BigDecimal("19.99"))
                        .withCurrency("USD")
                        .withAddress(address)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(new BigDecimal(16))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditRefundTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("10.95"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(new BigDecimal("10.95"))
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditRefundTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal("10.95"))
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(new BigDecimal("10.95"))
                    .withIdempotencyKey(idempotencyKey)
                    .withCurrency("USD")
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
    public void creditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .refund(new BigDecimal("10.95"))
                    .withCurrency("USD")
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

    @Test
    public void creditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(new BigDecimal("12.99"))
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(new BigDecimal("12.99"))
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
    public void creditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .reverse(new BigDecimal("12.99"))
                    .execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("TRANSACTION_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction to action cannot be found", ex.getMessage());
        }
    }

    @Test
    public void creditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("3.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        try {
            transaction.reverse(new BigDecimal("1.29")).execute("GpApiConfig");
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40006", ex.getResponseText());
            assertEquals("Status Code: 400 - partial reversal not supported", ex.getMessage());
        }
    }

    @Test
    public void creditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal("14"))
                        .withCurrency("USD")
                        .withMultiCapture(true)
                        .withAllowDuplicates(true)
                        .execute("GpApiConfig");

        assertNotNull(authorization);
        assertEquals(SUCCESS, authorization.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authorization.getResponseMessage());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal("3"))
                        .execute("GpApiConfig");

        assertNotNull(capture1);
        assertEquals(SUCCESS, capture1.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture1.getResponseMessage());

        Transaction capture2 =
                authorization.capture(new BigDecimal("5"))
                        .execute("GpApiConfig");

        assertNotNull(capture2);
        assertEquals(SUCCESS, capture2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture2.getResponseMessage());

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal("7"))
                        .execute("GpApiConfig");

        assertNotNull(capture3);
        assertEquals(SUCCESS, capture3.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture3.getResponseMessage());
    }

    @Test
    public void creditVerify() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void creditVerify_With_Address() throws ApiException {
        Address address = new Address();
        address.setPostalCode("WB3 A21");
        address.setStreetAddress1("Flat 456");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .withAddress(address)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertNotNull(response.getTransactionId());
    }

    @Test
    public void creditVerify_CP() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        ServicesContainer.configureService(config, "GpApiConfig");

        card.setCvn("123");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void creditVerify_CP_CVNNotMatched() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        ServicesContainer.configureService(config, "GpApiConfig");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("NOT_VERIFIED", response.getResponseCode());
        assertEquals("NOT_VERIFIED", response.getResponseMessage());
        assertEquals("NOT_MATCHED", response.getCvnResponseMessage());
    }

    @Test
    public void creditVerify_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency("USD")
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
    public void creditVerify_Without_Currency() throws ApiException {
        try {
            card
                    .verify()
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40041", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Validation failed for object='terminalConfig'. Error count: 1", ex.getMessage());
        }
    }

    @Test
    public void creditVerify_InvalidCVV() throws ApiException {
        card.setCvn("1234");
        try {
            card
                    .verify()
                    .withCurrency("EUR")
                    .execute("GpApiConfig");

        } catch (GatewayException ex) {
            assertEquals("40085", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Security Code/CVV2/CVC must be 3 digits", ex.getMessage());
        }
    }

    @Test
    public void creditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal("4.95"))
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .execute("GpApiConfig");

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal("4.95"))
                    .withCurrency("USD")
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

}