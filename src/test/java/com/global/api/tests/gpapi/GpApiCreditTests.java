package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiCreditTests extends BaseGpApiTest {

    private final CreditCardData card;

    public GpApiCreditTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void creditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(5))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal(2.99))
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        BigDecimal amount = new BigDecimal(10);

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal(amount.doubleValue() * 1.15))
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        BigDecimal amount = new BigDecimal(10);

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        try {
            transaction
                    .capture(new BigDecimal(amount.doubleValue() * 1.16))
                    .withGratuity(new BigDecimal(2))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("50020", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Can't settle for more than 115% of that which you authorised.", ex.getMessage());
        }
    }

    @Test
    public void creditAuthorizationAndCapture() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal("16"))
                        .withGratuity(new BigDecimal("2"))
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(new BigDecimal("16"))
                    .withIdempotencyKey(idempotencyKey)
                    .withGratuity(new BigDecimal("2"))
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
    public void creditAuthorization_CP() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setChannel(Channel.CardPresent.getValue());

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        CreditTrackData creditTrackData = new CreditTrackData();
        creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                creditTrackData
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

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

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        CreditTrackData creditTrackData = new CreditTrackData();
        creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);

        Transaction transaction =
                creditTrackData
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal(14))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

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
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", authorization.getTransactionId()), ex.getMessage());
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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditSale_WithoutPermissions() throws ApiException {
        String[] permissions = new String[]{"TRN_POST_Capture"};

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh")
                .setPermissions(permissions)
                .setChannel(Channel.CardNotPresent.getValue());

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        try {
            card
                    .charge(new BigDecimal("19.99"))
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("40212", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Permission not enabled to execute action", ex.getMessage());
        }
    }

    @Test
    public void creditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card.charge(new BigDecimal("19.99"))
                        .withCurrency("USD")
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getToken());
    }

    @Test
    public void creditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(new BigDecimal(16))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void creditRefundTransaction() throws ApiException {
        BigDecimal amount = new BigDecimal("10.95");

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(5.95))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(new BigDecimal(3.25))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        BigDecimal amount = new BigDecimal(5.95);

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        try {
            Transaction response =
                    transaction
                            .refund(new BigDecimal(amount.doubleValue() * 1.1))
                            .withCurrency("USD")
                            .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("40087", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - You may only refund up to 100% of the original amount.", ex.getMessage());
        }
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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(new BigDecimal("10.95"))
                    .withIdempotencyKey(idempotencyKey)
                    .withCurrency("USD")
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
    public void creditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .refund(new BigDecimal("10.95"))
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        }
    }

    @Test
    public void creditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(new BigDecimal("12.99"))
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(new BigDecimal("12.99"))
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
    public void creditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .reverse(new BigDecimal("12.99"))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        }
    }

    @Test
    public void creditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("3.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        try {
            transaction
                    .reverse(new BigDecimal("1.29"))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40214", ex.getResponseText());
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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(authorization);
        assertEquals(SUCCESS, authorization.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authorization.getResponseMessage());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal("3"))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture1);
        assertEquals(SUCCESS, capture1.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture1.getResponseMessage());

        Transaction capture2 =
                authorization.capture(new BigDecimal("5"))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture2);
        assertEquals(SUCCESS, capture2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture2.getResponseMessage());

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal("7"))
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

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

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card.setCvn("123");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

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

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency("USD")
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
    public void creditVerify_Without_Currency() throws ApiException {
        try {
            card
                    .verify()
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("40005", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("Status Code: 400 - Request expects the following fields currency", ex.getMessage());
        }
    }

    @Test
    public void creditVerify_InvalidCVV() throws ApiException {
        card.setCvn("1234");
        try {
            card
                    .verify()
                    .withCurrency("EUR")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("40085", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Security Code/CVV2/CVC must be 3 digits", ex.getMessage());
        }
    }

    @Test
    public void creditVerify_NotNumericCVV() throws ApiException {
        card.setCvn("SMA");
        try {
            card
                    .verify()
                    .withCurrency("EUR")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("50018", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - The line number 12 which contains '         [number] XXX [/number] ' does not conform to the schema", ex.getMessage());
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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal("4.95"))
                    .withCurrency("USD")
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

    // TODO: Check why it is failing
    @Test
    public void creditTrackDataVerify() throws ApiException {
        CreditTrackData creditTrackData = new CreditTrackData();
        creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");

        Transaction response =
                creditTrackData
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
    }

    @Test
    public void creditCardReauthorizeTransaction() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, "GP_API_CONFIG_CARD_PRESENT");

        CreditCardData card = new CreditCardData();
        card.setNumber("5425230000004415");
        card.setExpMonth(DateTime.now().getMonthOfYear());
        card.setExpYear(DateTime.now().getYear() + 1);
        card.setCvn("123");
        card.setCardHolderName("John Smith");


        Transaction chargeTransaction =
                card
                        .charge(new BigDecimal("1.25"))
                        .withCurrency("USD")
                        .execute("GP_API_CONFIG_CARD_PRESENT");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Transaction reverseTransaction =
                chargeTransaction
                        .reverse(new BigDecimal("1.25"))
                        .execute("GP_API_CONFIG_CARD_PRESENT");

        assertNotNull(reverseTransaction);
        assertEquals(SUCCESS, reverseTransaction.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverseTransaction.getResponseMessage());

        Transaction reauthTransaction =
                chargeTransaction
                        .reauthorize()
                        .execute("GP_API_CONFIG_CARD_PRESENT");

        assertNotNull(reauthTransaction);
        assertEquals(SUCCESS, reauthTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), reauthTransaction.getResponseMessage());
        assertEquals("00", reauthTransaction.getAuthorizationCode());
    }

}