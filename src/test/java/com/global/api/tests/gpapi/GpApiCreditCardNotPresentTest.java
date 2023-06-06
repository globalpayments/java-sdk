package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Customer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.StoredPaymentMethodSummaryPaged;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.logging.RequestFileLogger;
import lombok.SneakyThrows;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiCreditCardNotPresentTest extends BaseGpApiTest {

    private final CreditCardData card;
    private final BigDecimal amount = new BigDecimal("2.02");
    private final String currency = "USD";

    public GpApiCreditCardNotPresentTest() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardNotPresent);

        //DO NOT DELETE - usage example for some settings
//        HashMap<String, String> dynamicHeaders = new HashMap<String, String>() {{
//            put("x-gp-platform", "prestashop;version=1.7.2");
//            put("x-gp-extension", "coccinet;version=2.4.1");
//        }};
//
//        config.setDynamicHeaders(dynamicHeaders);

        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
//        config.setRequestLogger(new RequestFileLogger("C:\\temp\\GpApiCreditCardNotPresentTests.txt"));

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    @Test
    public void CreditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
    }

    @Test
    public void CreditSaleWithFingerPrint() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setCountry("US");
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ALWAYS");

        Transaction response =
                card
                        .charge(69)
                        .withCurrency("GBP")
                        .withAddress(address)
                        .withCustomerData(customer)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getFingerPrint());
        assertNotNull(response.getFingerPrintIndicator());
        assertEquals("EXISTS", response.getFingerPrintIndicator());
    }

    @Test
    public void VerifyTokenizedPaymentMethodWithFingerprint() throws ApiException {
        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ALWAYS");

        CreditCardData tokenizedCard = new CreditCardData();

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .withCustomerData(customer)
                        .execute(GP_API_CONFIG_NAME)
                        .getToken();

        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("GBP")
                        .withCustomerData(customer)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
        assertNotNull(response.getFingerPrint());
    }

    @Test
    public void CreditSaleWithFingerPrint_OnSuccess_WithDeclinedAuth() throws ApiException {
        card.setNumber("4000120000001154");

        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ON_SUCCESS");

        Transaction response =
                card
                        .charge(2)
                        .withCurrency("GBP")
                        .withCustomerData(customer)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("", response.getFingerPrint());
        assertEquals("", response.getFingerPrintIndicator());
    }

    @Test
    public void UpdatePaymentToken() throws ApiException {
        StoredPaymentMethodSummaryPaged response =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 1)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        assertEquals(1, response.getResults().size());
        String pmtToken = response.getResults().get(0).getId();     // Check if id or other field
        assertNotNull(pmtToken);
        assertNotNull(pmtToken);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(pmtToken);
        tokenizedCard.setCardHolderName("James BondUp");
        tokenizedCard.setExpYear(DateTime.now().getYear() + 1);
        tokenizedCard.setExpMonth(DateTime.now().getMonthOfYear());
        tokenizedCard.setNumber("4263970000005262");

        Transaction response2 =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .execute(GP_API_CONFIG_NAME);

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    public void UpdatePaymentToken_UsageModeOnly() throws ApiException {
        StoredPaymentMethodSummaryPaged response =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 1)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute(GP_API_CONFIG_NAME);

        assertEquals(1, response.getResults().size());
        String pmtToken = response.getResults().get(0).getId();     // Check if id or other field
        assertNotNull(pmtToken);
        assertNotNull(pmtToken);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(pmtToken);
        tokenizedCard.setCardHolderName("James BondUp");
        tokenizedCard.setNumber("4263970000005262");

        Transaction response2 =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .execute(GP_API_CONFIG_NAME);

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    public void CardTokenizationThenUpdateAndThenCharge() throws ApiException {
        Transaction response =
                card
                        .tokenize(null, null)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                        .execute(GP_API_CONFIG_NAME);

        String tokenId = response.getToken();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("GpApi");

        Transaction responseUpdateToken =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(responseUpdateToken);
        assertEquals("SUCCESS", responseUpdateToken.getResponseCode());
        assertEquals("ACTIVE", responseUpdateToken.getResponseMessage());
        assertEquals("MULTIPLE", responseUpdateToken.getTokenUsageMode().getValue());

        Transaction chargeResponse =
                tokenizedCard
                        .charge(1)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(chargeResponse);
        assertEquals("SUCCESS", chargeResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeResponse.getResponseMessage());
    }

    @Test
    public void CardTokenizationThenUpdateToSingleUsage() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50020", e.getResponseText());
            assertEquals("Status Code: 400 - Tokentype can only be MULTI", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CardTokenizationThenUpdateWithoutUsageMode() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50021", e.getResponseText());
            assertEquals("Status Code: 400 - Mandatory Fields missing [card expdate] See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditAuthorizationWithPaymentLinkId() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId("LNK_W1xgWehivDP8P779cFDDTZwzL01EEw4")
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
    }

    @Test
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(5)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(2.99)
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(amount.doubleValue() * 1.15)
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(amount.doubleValue() * 1.16)
                    .withGratuity(new BigDecimal(2))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("50020", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Can't settle for more than 115% of that which you authorised ", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditAuthorizationAndCapture() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(new BigDecimal(16))
                        .withGratuity(new BigDecimal(2))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    public void CreditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

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
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditAuthorization_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            card
                    .authorize(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            authorization
                    .capture(amount)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", authorization.getTransactionId()), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSale() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setPostalCode("12345");

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(address)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getPayerDetails());
    }

    @Test
    public void CreditSale_ONSuccess() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertTrue(response.getToken().startsWith("PMT_"));

        String generatedToken = response.getToken();

        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setToken(generatedToken);
        Transaction transaction =
                creditCardData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);
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
                .setChannel(Channel.CardNotPresent);

        final String GP_API_CONFIG_NAME_WITHOUT_PERMISSIONS = "GpApiConfig_WithoutPermissions";

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME_WITHOUT_PERMISSIONS);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal("19.99"))
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
                card.charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertNotNull(response.getToken());
    }

    @Test
    public void CreditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("5.95"))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction_Refund_AcceptedHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount.doubleValue() * 1.1)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount.doubleValue() * 1.2)
                    .withCurrency("USD")
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40087", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - You may only refund up to 115% of the original amount ", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditRefundTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            charge
                    .refund(amount)
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditRefund_ZeroAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .refund(0)
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50020", ex.getResponseText());
            assertEquals("Status Code: 400 - Zero negative or insufficient amount specified ", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction reverse =
                transaction
                        .reverse(amount)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(reverse, TransactionStatus.Reversed);
    }

    @Test
    public void CreditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditSale_WithDynamicDescriptor() throws ApiException {
        var dynamicDescriptor = "My company";
        var response =
                card
                        .charge(50)
                        .withCurrency("EUR")
                        .withDynamicDescriptor(dynamicDescriptor)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            charge
                    .reverse(amount)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals(String.format("Status Code: 404 - Transaction %s not found at this location.", charge.getTransactionId()), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("3.99"))
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(new BigDecimal("1.29"))
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40214", ex.getResponseText());
            assertEquals("Status Code: 400 - partial reversal not supported", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(authorization, TransactionStatus.Preauthorized);
        assertTrue(authorization.isMultiCapture());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal(3))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture1, TransactionStatus.Captured);

        Transaction capture2 =
                authorization.capture(new BigDecimal(5))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture2, TransactionStatus.Captured);

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal(7))
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(capture3, TransactionStatus.Captured);
    }

    @Test
    public void CreditVerify() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
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
                        .withCurrency(currency)
                        .withAddress(address)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertNotNull(response.getTransactionId());
    }

    @Test
    public void CreditVerify_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
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
    public void CreditVerify_Without_Currency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40005", ex.getResponseText());
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("Status Code: 400 - Request expects the following fields currency", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditVerify_InvalidCVV() throws ApiException {
        card.setCvn("1234");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40085", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - Security Code/CVV2/CVC must be 3 digits", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditVerify_NotNumericCVV() throws ApiException {
        card.setCvn("SMA");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .execute(GP_API_CONFIG_NAME);

        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("50018", ex.getResponseText());
            assertEquals("SYSTEM_ERROR_DOWNSTREAM", ex.getResponseCode());
            assertEquals("Status Code: 502 - The line number 12 which contains '         [number] XXX [/number] ' does not conform to the schema", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + transaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditChargeTransactions_WithDifferentIdempotencyKey() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(UUID.randomUUID().toString())
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(UUID.randomUUID().toString())
                        .execute(GP_API_CONFIG_NAME);
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertNotEquals(response.getTransactionId(), transaction.getTransactionId());
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

    @Test
    public void CreditSale_For_Android() throws ApiException {
        // GP-API settings for Android SDK
        GpApiConfig config =
                new GpApiConfig()
                        .setAppId(APP_ID)
                        .setAppKey(APP_KEY)
                        .setAndroid(true);

        config.setEnableLogging(true);

        final String GP_API_CONFIG_FOR_ANDROID_SDK = "GP_API_CONFIG_FOR_ANDROID_SDK";
        ServicesContainer.configureService(config, GP_API_CONFIG_FOR_ANDROID_SDK);

        card
                .charge(amount)
                .withCurrency(currency)
                .execute(GP_API_CONFIG_FOR_ANDROID_SDK);
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

}