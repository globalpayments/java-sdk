package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiCreditWithMerchantIdTests extends BaseGpApiTest {

    private CreditCardData card;
    private final String merchantId = "MER_c4c0df11039c48a9b63701adeaa296c3";

    @Before
    public void initialize() throws ConfigurationException {

        GpApiConfig config = new GpApiConfig();
        config.setAppId("zKxybfLqH7vAOtBQrApxD5AUpS3ITaPz");
        config.setAppKey("GAMlgEojm6hxZTLI");
        config.setChannel(Channel.CardNotPresent.getValue());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setTokenizationAccountName("Tokenization");

        config.setAccessTokenInfo(accessTokenInfo);

        config.setMerchantId(merchantId);
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");
        config.setEnableLogging(true);

//      DO NOT DELETE - usage example for some settings
//        HashMap<String, String> dynamicHeaders = new HashMap<String, String>() {{
//            put("x-gp-platform", "prestashop;version=1.7.2");
//            put("x-gp-extension", "coccinet;version=2.4.1");
//        }};
//
//        config.setDynamicHeaders(dynamicHeaders);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("852");
    }
    
    @Test
    public void CreditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal("14"))
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
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal("5"))
                        .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(new BigDecimal("2.99"))
                        .withGratuity(new BigDecimal("2"))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

@Test
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        BigDecimal amount = new BigDecimal("10");
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
                        .withGratuity(new BigDecimal("2"))
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        BigDecimal amount = new BigDecimal("10");
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
            Transaction capture =
                    transaction
                            .capture(new BigDecimal(amount.doubleValue() * 1.16))
                            .withGratuity(new BigDecimal("2"))
                            .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("50020", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Can't settle for more than 115% of that which you authorised ", ex.getMessage());
        }
    }

    @Test
    public void CreditSale() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setCountry("US");
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
    public void CreditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card
                        .charge(new BigDecimal("19.99"))
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
                        .refund(new BigDecimal("16"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditRefundTransaction() throws ApiException {
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
    public void CreditRefundTransaction_WithIdempotencyKey() throws ApiException {
        BigDecimal amount = new BigDecimal("10.95");
        String idempotencyKey = UUID.randomUUID().toString();

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
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency("USD")
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + response.getTransactionId() + ", status=CAPTURED", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

@Test
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("5.95"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(new BigDecimal("3.25"))
                        .withCurrency("USD")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        BigDecimal amount = new BigDecimal("5.95");
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
        }
        catch (GatewayException ex) {
            assertEquals("40087", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - You may only refund up to 100% of the original amount ", ex.getMessage());
        }
    }

@Test
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("10.95"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        transaction.setTransactionId("TRN_wrongid3213213");
        transaction.setToken("TRN_wrongid3213213");

        try {
            transaction
                    .refund(new BigDecimal("10.95"))
                    .withCurrency("USD")
                    //.withAllowDuplicates(true)
                    .execute(GP_API_CONFIG_NAME);
        }
        catch (GatewayException ex) {
            assertEquals("40008", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction " + transaction.getTransactionId() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    public void CreditReverseTransaction() throws ApiException {
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
    public void CreditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(new BigDecimal("12.99"))
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), response.getResponseMessage());

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
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + response.getTransactionId() + ", status=REVERSED", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        transaction.setTransactionId("TRN_wrongid3213213");
        transaction.setToken("TRN_wrongid3213213");

        try {
            transaction
                    .refund(new BigDecimal("10.95"))
                    .withCurrency("USD")
                    //.withAllowDuplicates(true)
                    .execute(GP_API_CONFIG_NAME);
        }
        catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + transaction.getTransactionId() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    public void CreditPartialReverseTransaction() throws ApiException {
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
    public void CreditAuthorizationForMultiCapture() throws ApiException {
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
                authorization
                        .capture(new BigDecimal("5"))
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
    public void CreditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

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

        Transaction capture =
                authorization
                        .capture(new BigDecimal("14"))
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            authorization
                    .capture(new BigDecimal("14"))
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + capture.getTransactionId() + ", status=CAPTURED", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCaptureWrongId() throws ApiException {
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

        authorization.setTransactionId("TRN_wrongid3213213");
        authorization.setToken("TRN_wrongid3213213");

        try {
            authorization
                    .capture(new BigDecimal("3"))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + authorization.getTransactionId() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    public void SaleWithTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("17.01"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardTokenizationThenPayingWithToken_SingleToMultiUse() throws ApiException {
        String token = card.tokenize(true, GP_API_CONFIG_NAME, PaymentMethodUsageMode.SINGLE);

        assertNotNull(token);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setCardHolderName("James Mason");

        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("17.01"))
                        .withCurrency("USD")
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertTrue(response.getToken().startsWith("PMT_"));

        tokenizedCard.setToken(response.getToken());

        tokenizedCard
                .charge(10)
                .withCurrency("USD")
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
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
    public void CreditVerify_WithAddress() throws ApiException {
        Address address = new Address();
        address.setPostalCode("750241234");
        address.setStreetAddress1("6860 Dallas Pkwy");

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .withAddress(address)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
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
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + response.getTransactionId() + ", status=VERIFIED", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditVerify_WithoutCurrency() {
        try {
            Transaction response =
                    card
                            .verify()
                            .execute(GP_API_CONFIG_NAME);
        }
        catch (GatewayException ex) {
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields currency", ex.getMessage());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void CreditVerify_InvalidCVV() {
        card.setCvn("1234");

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        }
        catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40085", ex.getResponseText());
            assertEquals("Status Code: 400 - Security Code/CVV2/CVC must be 3 digits", ex.getMessage());
        } catch (ApiException e) {
            e.printStackTrace();
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditVerify_NotNumericCVV() {
        card.setCvn("SMA");

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        }
        catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("50018", ex.getResponseText());
            assertEquals("Status Code: 502 - The line number 12 which contains '         [number] XXX [/number] ' does not conform to the schema", ex.getMessage());
        } catch (ApiException e) {
            e.printStackTrace();
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditVerify_CP() throws ApiException {

        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setEnvironment(Environment.TEST);
        gpApiConfig.setAppId("zKxybfLqH7vAOtBQrApxD5AUpS3ITaPz");
        gpApiConfig.setAppKey("GAMlgEojm6hxZTLI");
        gpApiConfig.setSecondsToExpire(60);
        gpApiConfig.setChannel(Channel.CardPresent.getValue());
        gpApiConfig.setMerchantId(merchantId);

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName( "transaction_processing");
        accessTokenInfo.setTokenizationAccountName("transaction_processing");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME + "CreditVerify_CP");
        card.setCardPresent(true);
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

    @Ignore
    @Test
    public void CreditSaleWithEntryMethod() throws ApiException {
        for (EntryMethod entryMethod : EntryMethod.values()){

            GpApiConfig gpApiConfig = new GpApiConfig();
            gpApiConfig.setEnvironment(Environment.TEST);
            gpApiConfig.setAppId("zKxybfLqH7vAOtBQrApxD5AUpS3ITaPz");
            gpApiConfig.setAppKey("GAMlgEojm6hxZTLI");
            gpApiConfig.setSecondsToExpire(60);
            gpApiConfig.setChannel(Channel.CardPresent.getValue());
            gpApiConfig.setMerchantId(merchantId);

            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
            accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
            accessTokenInfo.setTokenizationAccountName("transaction_processing");
            gpApiConfig.setAccessTokenInfo(accessTokenInfo);

            gpApiConfig.setEnableLogging(true);

            ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME + entryMethod);

            CreditTrackData creditTrackData = new CreditTrackData();
            creditTrackData.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
            creditTrackData.setEntryMethod(entryMethod);

            Transaction response =
                    creditTrackData
                            .charge(11)
                            .withCurrency("USD")
                            .execute(GP_API_CONFIG_NAME);

            assertNotNull(response);
            assertEquals(SUCCESS, response.getResponseCode());
            assertEquals("CAPTURED", response.getResponseMessage());
        }
    }

    @Ignore
    @Test
    //To be removed
    public void CreditVerify_CP_CVNNotMatched() throws ApiException {

        GpApiConfig gpApiConfig = new GpApiConfig();
        gpApiConfig.setEnvironment(Environment.TEST);
        gpApiConfig.setAppId("zKxybfLqH7vAOtBQrApxD5AUpS3ITaPz");
        gpApiConfig.setAppKey("GAMlgEojm6hxZTLI");
        gpApiConfig.setSecondsToExpire(60);
        gpApiConfig.setChannel(Channel.CardPresent.getValue());
        gpApiConfig.setMerchantId(merchantId);

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setTokenizationAccountName("transaction_processing");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        gpApiConfig.setEnableLogging(true);

        ServicesContainer.configureService(gpApiConfig, GP_API_CONFIG_NAME);

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
    public void CreditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction1 =
                card
                        .charge(4.95)
                        .withCurrency("USD")
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(transaction1);
        assertEquals(SUCCESS, transaction1.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction1.getResponseMessage());

        try {
            card
                    .charge(4.95)
                    .withCurrency("USD")
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction1.getTransactionId() + ", status=CAPTURED", ex.getMessage());
        }
    }

    @Test
    public void CreditVerify_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response =
                card
                        .verify()
                        .withCurrency("USD")
                        .withStoredCredential(storedCredential)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
    }

    @Test
    public void CreditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response =
                card
                        .charge(15.25)
                        .withCurrency("USD")
                        .withStoredCredential(storedCredential)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditSale_WithStoredCredentials_RecurringPayment() throws ApiException {
        BigDecimal amount = new BigDecimal(15.00);
        String currency = "USD";

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setNumber("4263970000005262");
        creditCardData.setExpMonth(expMonth);
        creditCardData.setExpYear(expYear);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(creditCardData.tokenize(GP_API_CONFIG_NAME));

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals("ENROLLED", secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getMessageType());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNull(secureEcom.getEci());

        BrowserData browserData = new BrowserData();
        browserData.setAcceptHeader("text/html,application/xhtml+xml,application/xml;q=9,image/webp,img/apng,*/*;q=0.8");
        browserData.setColorDepth(ColorDepth.TwentyFourBit);
        browserData.setIpAddress("123.123.123.123");
        browserData.setJavaEnabled(true);
        browserData.setLanguage("en");
        browserData.setScreenHeight(1080);
        browserData.setScreenWidth(1920);
        browserData.setChallengeWindowSize(ChallengeWindowSize.Windowed_600x400);
        browserData.setTimezone("0");
        browserData.setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64, x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");

        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withBrowserData(browserData)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals("ENROLLED", initAuth.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, initAuth.getVersion());
        assertEquals("SUCCESS_AUTHENTICATED", initAuth.getStatus());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertNotNull(initAuth.getChallengeReturnUrl());
        assertNotNull(initAuth.getMessageType());
        assertNotNull(initAuth.getSessionDataFieldName());

        tokenizedCard.setThreeDSecure(initAuth);

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        // assertNotNull(response.getCardBrandTransactionId());

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response2 =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response2);
        assertEquals(SUCCESS, response2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response2.getResponseMessage());
    }

}
