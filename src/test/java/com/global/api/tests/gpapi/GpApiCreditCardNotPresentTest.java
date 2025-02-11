package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.StoredPaymentMethodSummaryPaged;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import lombok.SneakyThrows;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiCreditCardNotPresentTest extends BaseGpApiTest {

    private final CreditCardData card;
    private final BigDecimal amount = new BigDecimal("2.02");
    private final String currency = "USD";

    public GpApiCreditCardNotPresentTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    @Test
    @Order(1)
    public void CreditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
        assertEquals("123456", transaction.getAuthorizationCode());
    }

    @Test
    @Order(2)
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
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getFingerPrint());
        assertNotNull(response.getFingerPrintIndicator());
        assertEquals("EXISTS", response.getFingerPrintIndicator());
        assertEquals("123456", response.getAuthorizationCode());
    }

    @Test
    @Order(3)
    public void VerifyTokenizedPaymentMethodWithFingerprint() throws ApiException {
        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ALWAYS");

        CreditCardData tokenizedCard = new CreditCardData();

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .withCustomerData(customer)
                        .execute()
                        .getToken();

        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("GBP")
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
        assertNotNull(response.getFingerPrint());
    }

    @Test
    @Order(4)
    public void CreditSaleWithFingerPrint_OnSuccess_WithDeclinedAuth() throws ApiException {
        card.setNumber("4000120000001154");

        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ON_SUCCESS");

        Transaction response =
                card
                        .charge(2)
                        .withCurrency("GBP")
                        .withCustomerData(customer)
                        .execute();

        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("", response.getFingerPrint());
        assertEquals("", response.getFingerPrintIndicator());
    }

    @Test
    @Order(5)
    public void CreditSaleWithFingerPrint_InvalidValueForFingerPrint() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setCountry("US");
        address.setPostalCode("12345");

        Customer customer = new Customer();
        customer.setDeviceFingerPrint("NO");

        boolean exceptionCaught = false;
        try {
            card
                    .charge(69)
                    .withCurrency("GBP")
                    .withAddress(address)
                    .withCustomerData(customer)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - fingerprint_mode contains unexpected data", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(6)
    public void CreditSaleWithSurcharge() throws ApiException {
        Transaction response =
                card
                        .charge(69)
                        .withCurrency("GBP")
                        .withSurchargeAmount(new BigDecimal("3"), CreditDebitIndicator.Credit)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertEquals("123456", response.getAuthorizationCode());
    }

    @Test
    @Order(7)
    public void CreditSaleWithExceededSurcharge() throws ApiException {
        boolean exceptionCaught = false;

        try {
            card
                    .charge(69)
                    .withCurrency("GBP")
                    .withSurchargeAmount(new BigDecimal("4"), CreditDebitIndicator.Credit)
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - The surcharge amount is greater than 5% of the transaction amount", e.getMessage());
            assertEquals("50020", e.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(8)
    public void UpdatePaymentToken() throws ApiException {
        StoredPaymentMethodSummaryPaged response =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 1)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();

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
                        .execute();

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    @Order(9)
    public void UpdatePaymentToken_UsageModeOnly() throws ApiException {
        StoredPaymentMethodSummaryPaged response =
                ReportingService
                        .findStoredPaymentMethodsPaged(1, 1)
                        .orderBy(StoredPaymentMethodSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();

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
                        .execute();

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    @Order(10)
    public void CardTokenizationThenUpdateAndThenCharge() throws ApiException {
        String[] permissions = new String[]{"PMT_POST_Create_Single"};

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setPermissions(permissions);

        ServicesContainer.configureService(config, "singleUseToken");

        Transaction response =
                card
                        .tokenize(null, null)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                        .execute("singleUseToken");

        String tokenId = response.getToken();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("GpApi");

        Transaction responseUpdateToken =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .execute();

        assertNotNull(responseUpdateToken);
        assertEquals("SUCCESS", responseUpdateToken.getResponseCode());
        assertEquals("ACTIVE", responseUpdateToken.getResponseMessage());
        assertEquals("MULTIPLE", responseUpdateToken.getTokenUsageMode().getValue());

        Transaction chargeResponse =
                tokenizedCard
                        .charge(1)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(chargeResponse);
        assertEquals("SUCCESS", chargeResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeResponse.getResponseMessage());
    }

    @Test
    @Order(11)
    public void CardTokenizationThenUpdateToSingleUsage() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50020", e.getResponseText());
            assertEquals("Status Code: 400 - Tokentype can only be MULTI", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(12)
    public void CardTokenizationThenUpdateWithoutUsageMode() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + UUID.randomUUID());

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50021", e.getResponseText());
            assertEquals("Status Code: 400 - Mandatory Fields missing [card expdate] See Developers Guide", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(13)
    public void CreditAuthorizationWithPaymentLinkId() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId("LNK_W1xgWehivDP8P779cFDDTZwzL01EEw4")
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
    }

    @Test
    @Order(14)
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(5)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(2.99)
                        .withGratuity(new BigDecimal(2))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    @Order(15)
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(amount.doubleValue() * 1.15)
                        .withGratuity(new BigDecimal(2))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    @Order(16)
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(amount.doubleValue() * 1.16)
                    .withGratuity(new BigDecimal(2))
                    .execute();
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
    @Order(17)
    public void CreditAuthorizationAndCapture() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(new BigDecimal(16))
                        .withGratuity(new BigDecimal(2))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    @Order(18)
    public void CreditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(new BigDecimal(16))
                    .withIdempotencyKey(idempotencyKey)
                    .withGratuity(new BigDecimal(2))
                    .execute();
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
    @Order(19)
    public void CreditAuthorization_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            card
                    .authorize(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
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
    @Order(20)
    public void CreditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            authorization
                    .capture(amount)
                    .execute();
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
    @Order(21)
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
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getPayerDetails());
    }

    @Test
    @Order(22)
    public void CreditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

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
                        .withStoredCredential(storedCredential)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getPayerDetails());
    }

    @Test
    @Order(23)
    public void CreditSale_MissingStoredCredentialInitiator() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withStoredCredential(storedCredential)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following conditionally mandatory fields initiator, model and sequence.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40007", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(24)
    public void CreditSale_MissingStoredCredentialType() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withStoredCredential(storedCredential)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following conditionally mandatory fields initiator, model and sequence.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40007", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(25)
    public void CreditSale_MissingStoredCredentialSequence() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Recurring);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withStoredCredential(storedCredential)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("Status Code: 400 - Request expects the following conditionally mandatory fields initiator, model and sequence.", ex.getMessage());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40007", ex.getResponseText());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(26)
    public void CreditSale_ONSuccess() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute();
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
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);
    }

    @SneakyThrows
    @Test
    @Order(27)
    public void CreditSale_WithoutPermissions() {
        String[] permissions = new String[]{"TRN_POST_Capture"};

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setPermissions(permissions);

        final String _WITHOUT_PERMISSIONS = "GpApiConfig_WithoutPermissions";

        ServicesContainer.configureService(config, _WITHOUT_PERMISSIONS);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal("19.99"))
                    .withCurrency("USD")
                    .execute(_WITHOUT_PERMISSIONS);
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
    @Order(28)
    public void CreditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card.charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertNotNull(response.getToken());
    }

    @Test
    @Order(29)
    public void CreditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Order(30)
    public void CreditRefundTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Order(31)
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("5.95"))
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Order(32)
    public void CreditRefundTransaction_Refund_AcceptedHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount.doubleValue() * 1.1)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Order(33)
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount.doubleValue() * 1.2)
                    .withCurrency("USD")
                    .execute();
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
    @Order(34)
    public void CreditRefundTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .withCurrency(currency)
                    .execute();
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
    @Order(35)
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            charge
                    .refund(amount)
                    .withCurrency(currency)
                    .execute();

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
    @Order(36)
    public void CreditRefund_ZeroAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .refund(0)
                    .withCurrency(currency)
                    .execute();

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
    @Order(37)
    public void CreditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction reverse =
                transaction
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverse, TransactionStatus.Reversed);
    }

    @Test
    @Order(38)
    public void CreditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
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
    @Order(39)
    public void CreditSale_WithDynamicDescriptor() throws ApiException {
        var dynamicDescriptor = "My company";
        var response =
                card
                        .charge(50)
                        .withCurrency("EUR")
                        .withDynamicDescriptor(dynamicDescriptor)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(40)
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            charge
                    .reverse(amount)
                    .execute();
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
    @Order(41)
    public void CreditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("3.99"))
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(new BigDecimal("1.29"))
                    .execute();
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
    @Order(42)
    public void CreditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .execute();
        assertTransactionResponse(authorization, TransactionStatus.Preauthorized);
        assertTrue(authorization.isMultiCapture());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal(3))
                        .execute();
        assertTransactionResponse(capture1, TransactionStatus.Captured);

        Transaction capture2 =
                authorization.capture(new BigDecimal(5))
                        .execute();
        assertTransactionResponse(capture2, TransactionStatus.Captured);

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal(7))
                        .execute();
        assertTransactionResponse(capture3, TransactionStatus.Captured);
    }

    @Test
    @Order(43)
    public void CreditVerify() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    @Order(44)
    public void CreditVerify_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    @Order(45)
    public void CreditVerify_With_Address() throws ApiException {
        Address address = new Address();
        address.setPostalCode("WB3 A21");
        address.setStreetAddress1("Flat 456");

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withAddress(address)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertNotNull(response.getTransactionId());
    }

    @Test
    @Order(46)
    public void CreditVerify_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
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
    @Order(47)
    public void VerifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .execute()
                        .getToken();

        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("GBP")
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
    }

    @Test
    @Order(48)
    public void CreditVerify_Without_Currency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .execute();

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
    @Order(49)
    public void CreditVerify_InvalidCVV() throws ApiException {
        card.setCvn("1234");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .execute();

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
    @Order(50)
    public void CreditVerify_NotNumericCVV() throws ApiException {
        card.setCvn("SMA");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .execute();

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
    @Order(51)
    public void CreditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
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
    @Order(52)
    public void CreditChargeTransactions_WithDifferentIdempotencyKey() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(UUID.randomUUID().toString())
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(UUID.randomUUID().toString())
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertNotEquals(response.getTransactionId(), transaction.getTransactionId());
    }

    @Test
    @Order(53)
    public void CreditSale_ExpiryCard() throws ApiException {
        card.setExpYear(DateTime.now().getYear() - 1);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal(14))
                    .withCurrency("USD")
                    .withAllowDuplicates(true)
                    .execute();
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
    @Order(54)
    public void CreditSale_For_Android() throws ApiException {
        // GP-API settings for Android SDK
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setAndroid(true);

        final String GP_API_CONFIG_FOR_ANDROID_SDK = "GP_API_CONFIG_FOR_ANDROID_SDK";
        ServicesContainer.configureService(config, GP_API_CONFIG_FOR_ANDROID_SDK);

        Transaction response = card
                .charge(amount)
                .withCurrency(currency)
                .execute(GP_API_CONFIG_FOR_ANDROID_SDK);

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

}
