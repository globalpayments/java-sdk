package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiCreditTests extends BaseGpApiTest {

    private final CreditCardData card;
    private CreditTrackData creditTrackData;

    public GpApiCreditTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent.getValue());

        //DO NOT DELETE - usage example for some settings
//        HashMap<String, String> dynamicHeaders = new HashMap<String, String>() {{
//            put("x-gp-platform", "prestashop;version=1.7.2");
//            put("x-gp-extension", "coccinet;version=2.4.1");
//        }};
//
//        config.setDynamicHeaders(dynamicHeaders);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    private void GpApiCardPresentConfig() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME_CARD_PRESENT);

        creditTrackData = new CreditTrackData();
        creditTrackData.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);
    }

    @Test
    public void CreditAuthorization() throws ApiException {
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
    public void CreditAuthorizationWithPaymentLinkId() throws ApiException {
        var transaction =
                card
                        .authorize(14)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withPaymentLinkId("LNK_W1xgWehivDP8P779cFDDTZwzL01EEw4")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
    }

    @Test
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
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
            assertEquals("Status Code: 400 - Can't settle for more than 115% of that which you authorised ", ex.getMessage());
        }
    }

    @Test
    public void CreditAuthorizationAndCapture() throws ApiException {
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
                        .capture(new BigDecimal(16))
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void CreditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
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
                    .capture(new BigDecimal(16))
                    .withIdempotencyKey(idempotencyKey)
                    .withGratuity(new BigDecimal(2))
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
    public void CreditAuthorization_CP() throws ApiException {
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
    public void CreditAuthorizationAndCapture_CP() throws ApiException {
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
    public void CreditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId(UUID.randomUUID().toString());

        try {
            authorization
                    .capture(new BigDecimal(16))
                    .withGratuity(new BigDecimal(2))
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", authorization.getTransactionId()), ex.getMessage());
        }
    }

    @Test
    public void CreditSale() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");

        BigDecimal amount = new BigDecimal("19.99");

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency("USD")
                        .withAddress(address)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());

        assertEquals(amount, response.getBalanceAmount());
    }

    @Test
    public void CreditSaleContactlessSwipe() throws ApiException {
        GpApiConfig config = new GpApiConfig();

        config.setAppId("JF2GQpeCrOivkBGsTRiqkpkdKp67Gxi0");
        config.setAppKey("y7vALnUtFulORlTV");
        config.setSecondsToExpire(60);
        config.setChannel(Channel.CardPresent.getValue());

        final String CONFIG_NAME = GP_API_CONFIG_NAME + "_CONTACTLESS_SWIPE";

        ServicesContainer.configureService(config, CONFIG_NAME);

        var card = new CreditTrackData();
        card.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");

        var tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390191FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        var response =
                card
                        .charge(10)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withTagData(tagData)
                        .execute(CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @SneakyThrows
    @Test
    public void CreditSale_WithoutPermissions() {
        String[] permissions = new String[]{"TRN_POST_Capture"};

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setPermissions(permissions)
                .setChannel(Channel.CardPresent.getValue());

        final String GP_API_CONFIG_NAME_WITHOUT_PERMISSIONS = "GpApiConfig_WithoutPermissions";

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME_WITHOUT_PERMISSIONS);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal(19.99))
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME_WITHOUT_PERMISSIONS);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40212", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Permission not enabled to execute action", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card.charge(new BigDecimal(19.99))
                        .withCurrency("USD")
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getToken());
    }

    @Test
    public void CreditRefund() throws ApiException {
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
    public void CreditRefundTransaction() throws ApiException {
        BigDecimal amount = new BigDecimal(10.95);

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
            transaction
                    .refund(new BigDecimal(amount.doubleValue() * 1.1))
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("40087", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - You may only refund up to 100% of the original amount ", ex.getMessage());
        }
    }

    @Test
    public void CreditRefundTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal(10.95))
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
                    .refund(new BigDecimal(10.95))
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
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .refund(new BigDecimal(10.95))
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        }
    }

    @Test
    public void CreditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(12.99))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(new BigDecimal(12.99))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal(12.99))
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
                    .reverse(new BigDecimal(12.99))
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
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        try {
            charge
                    .reverse(new BigDecimal(12.99))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        }
    }

    @Test
    public void CreditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal(3.99))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        try {
            transaction
                    .reverse(new BigDecimal(1.29))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40214", ex.getResponseText());
            assertEquals("Status Code: 400 - partial reversal not supported", ex.getMessage());
        }
    }

    @Test
    public void CreditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency("USD")
                        .withMultiCapture(true)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(authorization);
        assertEquals(SUCCESS, authorization.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authorization.getResponseMessage());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal(3))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture1);
        assertEquals(SUCCESS, capture1.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture1.getResponseMessage());

        Transaction capture2 =
                authorization.capture(new BigDecimal(5))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture2);
        assertEquals(SUCCESS, capture2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture2.getResponseMessage());

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal(7))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture3);
        assertEquals(SUCCESS, capture3.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture3.getResponseMessage());
    }

    @Test
    public void CreditVerify() throws ApiException {
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
    public void CreditVerify_With_Address() throws ApiException {
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
    public void CreditVerify_CP() throws ApiException {
        GpApiCardPresentConfig();

        card.setCvn("123");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void CreditVerify_CP_CreditTrackData() throws ApiException {
        GpApiCardPresentConfig();

        Transaction response =
                creditTrackData
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void CreditVerify_CP_CVNNotMatched() throws ApiException {
        GpApiCardPresentConfig();

        card.setNumber("30450000000985");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(response);
        assertEquals("NOT_VERIFIED", response.getResponseCode());
        assertEquals("NOT_VERIFIED", response.getResponseMessage());
    }

    @Test
    public void CreditVerify_WithIdempotencyKey() throws ApiException {
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
    public void CreditVerify_Without_Currency() throws ApiException {
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
    public void CreditVerify_InvalidCVV() throws ApiException {
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
    public void CreditVerify_NotNumericCVV() throws ApiException {
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
    public void CreditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(new BigDecimal(4.95))
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal(4.95))
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
    public void CreditTrackDataVerify() throws ApiException {
        GpApiCardPresentConfig();

        Transaction response =
                creditTrackData
                        .verify()
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
    }

    @Test
    public void CreditCardReauthorizeTransaction() throws ApiException {
        GpApiCardPresentConfig();
        BigDecimal amount = new BigDecimal(1.25);

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Transaction reverseTransaction =
                chargeTransaction
                        .reverse(amount)
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reverseTransaction);
        assertEquals(SUCCESS, reverseTransaction.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverseTransaction.getResponseMessage());

        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reAuthTransaction);
        assertEquals(SUCCESS, reAuthTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), reAuthTransaction.getResponseMessage());
        assertEquals("00", reAuthTransaction.getAuthorizationCode());
    }

    @Test
    public void CreditCardReauthorizeTransaction_OldExistentSale() throws ApiException {
        GpApiCardPresentConfig();

        Date startDate = DateUtils.addDays(new Date(), -29);
        Date endDate = DateUtils.addDays(new Date(), -1);

        TransactionSummaryPaged resultTransactions =
                ReportingService
                        .findTransactionsPaged(1, 1000)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.TransactionStatus, TransactionStatus.Preauthorized)
                        .and(SearchCriteria.Channel, Channel.CardPresent)
                        .and(SearchCriteria.CardNumberLastFour, "0016")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(resultTransactions);

        if (resultTransactions.results.size() > 0) {

            int random = new Random().nextInt(resultTransactions.results.size());

            Transaction transaction = new Transaction();
            transaction.setBalanceAmount(resultTransactions.results.get(random).getAmount());
            transaction.setTransactionId(resultTransactions.results.get(random).getTransactionId());

            Transaction reverseTransaction =
                    transaction
                            .reverse()
                            .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

            assertNotNull(reverseTransaction);
            assertEquals(SUCCESS, reverseTransaction.getResponseCode());
            assertEquals(TransactionStatus.Reversed.getValue(), reverseTransaction.getResponseMessage());

            Transaction reAuthTransaction =
                    reverseTransaction
                            .reauthorize()
                            .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

            assertNotNull(reAuthTransaction);
            assertEquals(SUCCESS, reAuthTransaction.getResponseCode());
            assertEquals(TransactionStatus.Preauthorized.getValue(), reAuthTransaction.getResponseMessage());
            assertEquals("00", reAuthTransaction.getAuthorizationCode());
        }
    }

    @Test
    public void CreditCardReauthorizeAuthorizedTransaction() throws ApiException {
        GpApiCardPresentConfig();

        Transaction authTransaction =
                card
                        .authorize(new BigDecimal(1.25))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(authTransaction);
        assertEquals(SUCCESS, authTransaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authTransaction.getResponseMessage());

        Transaction reverseTransaction =
                authTransaction
                        .reverse(new BigDecimal(1.25))
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reverseTransaction);
        assertEquals(SUCCESS, reverseTransaction.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverseTransaction.getResponseMessage());
        assertEquals(authTransaction.getTransactionId(), reverseTransaction.getTransactionId());

        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reAuthTransaction);
        assertEquals(SUCCESS, reAuthTransaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), reAuthTransaction.getResponseMessage());
        assertEquals("00", reAuthTransaction.getAuthorizationCode());
        assertNotEquals(reverseTransaction.getTransactionId(), reAuthTransaction.getTransactionId());
    }

    @Test
    public void CreditCardReauthorizeTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();
        GpApiCardPresentConfig();

        Transaction chargeTransaction =
                card
                        .charge(new BigDecimal(1.25))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Transaction reverseTransaction =
                chargeTransaction
                        .reverse(new BigDecimal(1.25))
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reverseTransaction);
        assertEquals(SUCCESS, reverseTransaction.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reverseTransaction.getResponseMessage());

        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(reAuthTransaction);
        assertEquals(SUCCESS, reAuthTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), reAuthTransaction.getResponseMessage());
        assertEquals("00", reAuthTransaction.getAuthorizationCode());

        boolean exceptionCaught = false;
        try {
            reverseTransaction
                    .reauthorize()
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME_CARD_PRESENT);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + reAuthTransaction.getTransactionId() + ", status=CAPTURED", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_Refund() throws ApiException {
        GpApiCardPresentConfig();

        Transaction refundTransaction =
                card
                        .refund(new BigDecimal(1.25))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(refundTransaction);
        assertEquals(SUCCESS, refundTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), refundTransaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            refundTransaction
                    .reauthorize()
                    .execute(GP_API_CONFIG_NAME_CARD_PRESENT);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - An error occurred on the server.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_SaleWithCapturedStatus() throws ApiException {
        GpApiCardPresentConfig();

        Transaction chargeTransaction =
                card
                        .charge(new BigDecimal(1.25))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME_CARD_PRESENT);

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            chargeTransaction
                    .reauthorize()
                    .execute(GP_API_CONFIG_NAME_CARD_PRESENT);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40044", ex.getResponseText());
            assertEquals("Status Code: 400 - 36, Invalid original transaction for reauthorization-This error is returned from a CreditAuth or CreditSale if the original transaction referenced by GatewayTxnId cannot be found. This is typically because the original does not meet the criteria for the sale or authorization by GatewayTxnID. This error can also be returned if the original transaction is found, but the card number has been written over with nulls after 30 days.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_NonExistentId() throws ApiException {
        GpApiCardPresentConfig();
        String randomTransactionId = UUID.randomUUID().toString();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(randomTransactionId);
        transaction.setBalanceAmount(new BigDecimal(1.25));

        boolean exceptionCaught = false;
        try {
            transaction
                    .reauthorize()
                    .execute(GP_API_CONFIG_NAME_CARD_PRESENT);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + randomTransactionId + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSale_ExpiryCard() throws ApiException {
        card.setExpYear(DateTime.now().getYear() - 1);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal(14))
                    .withCurrency("USD")
                    .withAllowDuplicates(true)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40085", ex.getResponseText());
            assertEquals("Status Code: 400 - Expiry date invalid", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

}