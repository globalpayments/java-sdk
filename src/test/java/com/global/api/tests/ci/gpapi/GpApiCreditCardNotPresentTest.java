package com.global.api.tests.ci.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.StoredPaymentMethodSummaryPaged;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.tests.utils.citesting.CiTestingHarness;
import com.global.api.utils.DateUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class GpApiCreditCardNotPresentTest {

    private static final String APP_ID = "4gPqnGBkppGYvoE5UX9EWQlotTxGUDbs";
    private static final String APP_KEY = "FQyJA5VuEQfcji2M";
    private static final String SUCCESS = "SUCCESS";
    private static final String DECLINED = "DECLINED";
    private static final String VERIFIED = "VERIFIED";
    protected static final CiTestingHarness ciTestingHarness = new CiTestingHarness(
            "https://apis.sandbox.globalpay.com/ucp",
            CiTestingHarness.CacheMode.Locked,
            "GpApiCreditCardNotPresentTest"
    );
    private static final int expMonth = ciTestingHarness.getCurrentTime().getMonthOfYear();
    private static final int expYear = ciTestingHarness.getCurrentTime().getYear() + 1;
    private static final Date startDate = DateUtils.atStartOfDay(DateUtils.addDays(ciTestingHarness.getCurrentTime().toDate(), -30));
    private static final Date endDate = DateUtils.atEndOfDay(ciTestingHarness.getCurrentTime().toDate());

    private final CreditCardData card;
    private final BigDecimal amount = new BigDecimal("2.02");
    private final BigDecimal amount2 = new BigDecimal("3.02");
    private final String currency = "USD";

    public GpApiCreditCardNotPresentTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setServiceUrl(ciTestingHarness.getTestingUrl());
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    @Test
    @Disabled
    public void CreditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
        assertEquals("123456", transaction.getAuthorizationCode());
    }

    @Test
    @Disabled
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSaleWithFingerPrint"))
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
    public void VerifyTokenizedPaymentMethodWithFingerprint() throws ApiException {
        Customer customer = new Customer();
        customer.setDeviceFingerPrint("ALWAYS");

        CreditCardData tokenizedCard = new CreditCardData();

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .withCustomerData(customer)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("verifyTokenizedPaymentMethodWithFingerprint_tokenize"))
                        .execute()
                        .getToken();

        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("GBP")
                        .withCustomerData(customer)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("verifyTokenizedPaymentMethodWithFingerprint_verify"))
                        .execute();

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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSaleWithFingerPrint_OnSuccess_WithDeclinedAuth"))
                        .execute();

        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("", response.getFingerPrint());
        assertEquals("", response.getFingerPrintIndicator());
    }

    @Test
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
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSaleWithFingerPrint_InvalidValueForFingerPrint"))
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
    public void CreditSaleWithSurcharge() throws ApiException {
        Transaction response =
                card
                        .charge(69)
                        .withCurrency("GBP")
                        .withSurchargeAmount(new BigDecimal("3"), CreditDebitIndicator.Credit)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSaleWithSurcharge"))
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertEquals("123456", response.getAuthorizationCode());
    }

    @Test
    @Disabled
    public void CreditSaleWithExceededSurcharge() throws ApiException {
        boolean exceptionCaught = false;

        try {
            card
                    .charge(69)
                    .withCurrency("GBP")
                    .withSurchargeAmount(new BigDecimal("4"), CreditDebitIndicator.Credit)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSaleWithExceededSurcharge"))
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
    @Disabled
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
        tokenizedCard.setExpYear(ciTestingHarness.getCurrentTime().getYear() + 1);
        tokenizedCard.setExpMonth(ciTestingHarness.getCurrentTime().getMonthOfYear());
        tokenizedCard.setNumber("4263970000005262");

        Transaction response2 =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("updatePaymentToken"))
                        .execute();

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    @Disabled
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("updatePaymentToken_UsageModeOnly"))
                        .execute();

        assertEquals("SUCCESS", response2.getResponseCode());
        assertEquals("ACTIVE", response2.getResponseMessage());
        assertEquals(pmtToken, response2.getToken());
        assertEquals(PaymentMethodUsageMode.MULTIPLE, response2.getTokenUsageMode());
    }

    @Test
    @Disabled
    public void CardTokenizationThenUpdateAndThenCharge() throws ApiException {
        String[] permissions = new String[]{"PMT_POST_Create_Single"};

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setPermissions(permissions);

        ServicesContainer.configureService(config, "singleUseToken");

        Transaction response =
                card
                        .tokenize(null, null)
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("cardTokenizationThenUpdateAndThenCharge_tokenize"))
                        .execute("singleUseToken");

        String tokenId = response.getToken();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(tokenId);
        tokenizedCard.setCardHolderName("GpApi");

        Transaction responseUpdateToken =
                tokenizedCard
                        .updateToken()
                        .withPaymentMethodUsageMode(PaymentMethodUsageMode.MULTIPLE)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("cardTokenizationThenUpdateAndThenCharge_update"))
                        .execute();

        assertNotNull(responseUpdateToken);
        assertEquals("SUCCESS", responseUpdateToken.getResponseCode());
        assertEquals("ACTIVE", responseUpdateToken.getResponseMessage());
        assertEquals("MULTIPLE", responseUpdateToken.getTokenUsageMode().getValue());

        Transaction chargeResponse =
                tokenizedCard
                        .charge(1)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("cardTokenizationThenUpdateAndThenCharge_charge"))
                        .execute();

        assertNotNull(chargeResponse);
        assertEquals("SUCCESS", chargeResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeResponse.getResponseMessage());
    }

    @Test
    @Disabled
    public void CardTokenizationThenUpdateToSingleUsage() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + ciTestingHarness.generateRandomId("CardTokenizationThenUpdateToSingleUsage_token"));

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .withPaymentMethodUsageMode(PaymentMethodUsageMode.SINGLE)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("cardTokenizationThenUpdateToSingleUsage"))
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50020", e.getResponseText());
            assertEquals("Status Code: 400 - TokenType can only be MULTI", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Disabled
    public void CardTokenizationThenUpdateWithoutUsageMode() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("PMT_" + ciTestingHarness.generateRandomId("CardTokenizationThenUpdateWithoutUsageMode_token"));

        boolean exceptionCaught = false;

        try {
            tokenizedCard
                    .updateToken()
                    .withClientTransactionId(ciTestingHarness.generateRandomId("cardTokenizationThenUpdateWithoutUsageMode"))
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("50021", e.getResponseText());
            assertEquals("Status Code: 400 - Mandatory Fields missing [card ExpDate] See Developers Guide", e.getMessage());
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationWithPaymentLinkId"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);
    }

    @Test
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(5))
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureLowerAmount_auth"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(2.99)
                        .withGratuity(new BigDecimal(2))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureLowerAmount_capture"))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureHigherAmount_auth"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(amount.doubleValue() * 1.15)
                        .withGratuity(new BigDecimal(2))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureHigherAmount_capture"))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }

    @Test
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureHigherAmount_WithError_auth"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(amount.doubleValue() * 1.16)
                    .withGratuity(new BigDecimal(2))
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorization_CaptureHigherAmount_WithError_capture"))
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
    public void CreditAuthorizationAndCapture() throws ApiException {
        Transaction transaction =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationAndCapture_auth"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction capture =
                transaction
                        .capture(new BigDecimal(16))
                        .withGratuity(new BigDecimal(2))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationAndCapture_capture"))
                        .execute();
        assertTransactionResponse(capture, TransactionStatus.Captured);
    }


    @Test
    public void CreditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId(ciTestingHarness.generateRandomId("CreditCaptureWrongId_txnId"));

        boolean exceptionCaught = false;
        try {
            authorization
                    .capture(amount)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditCaptureWrongId_capture"))
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getPayerDetails());
    }

    @Test
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_WithStoredCredentials"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertEquals(amount, response.getBalanceAmount());
        assertNull(response.getPayerDetails());
    }

    @Test
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
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_MissingStoredCredentialInitiator"))
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
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_MissingStoredCredentialType"))
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
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_MissingStoredCredentialSequence"))
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
    public void CreditSale_ONSuccess() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_ONSuccess_charge"))
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_ONSuccess_tokenCharge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);
    }

    @SneakyThrows
    @Test
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
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_WithoutPermissions"))
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
    public void CreditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card.charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_WithRequestMultiUseToken"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
        assertNotNull(response.getToken());
    }

    @Test
    public void CreditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefund"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_refund"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("5.95"))
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_RefundLowerAmount_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(new BigDecimal("3.25"))
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_RefundLowerAmount_refund"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditRefundTransaction_Refund_AcceptedHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount2)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_Refund_AcceptedHigherAmount_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund(amount2.doubleValue() * 1.1)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_Refund_AcceptedHigherAmount_refund"))
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    @Disabled
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_RefundHigherAmount_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount.doubleValue() * 1.2)
                    .withCurrency("USD")
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransaction_RefundHigherAmount_refund"))
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
    @Disabled
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(ciTestingHarness.generateRandomId("CreditRefundTransactionWrongId_txnId"));

        boolean exceptionCaught = false;
        try {
            charge
                    .refund(amount)
                    .withCurrency(currency)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefundTransactionWrongId_refund"))
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
    public void CreditRefund_ZeroAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .refund(0)
                    .withCurrency(currency)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditRefund_ZeroAmount"))
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
    public void CreditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditReverseTransaction_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction reverse =
                transaction
                        .reverse(amount)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditReverseTransaction_reverse"))
                        .execute();
        assertTransactionResponse(reverse, TransactionStatus.Reversed);
    }


    @Test
    public void CreditSale_WithDynamicDescriptor() throws ApiException {
        String dynamicDescriptor = "My company";
        Transaction response =
                card
                        .charge(50)
                        .withCurrency("EUR")
                        .withDynamicDescriptor(dynamicDescriptor)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_WithDynamicDescriptor"))
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction charge = new Transaction();
        charge.setTransactionId(ciTestingHarness.generateRandomId("CreditReverseTransactionWrongId_txnId"));

        boolean exceptionCaught = false;
        try {
            charge
                    .reverse(amount)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditReverseTransactionWrongId_reverse"))
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
    public void CreditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(new BigDecimal("3.99"))
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditPartialReverseTransaction_charge"))
                        .execute();
        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(new BigDecimal("1.29"))
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditPartialReverseTransaction_reverse"))
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
    public void CreditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal(14))
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationForMultiCapture_auth"))
                        .execute();
        assertTransactionResponse(authorization, TransactionStatus.Preauthorized);
        assertTrue(authorization.isMultiCapture());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal(3))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationForMultiCapture_capture1"))
                        .execute();
        assertTransactionResponse(capture1, TransactionStatus.Captured);

        Transaction capture2 =
                authorization.capture(new BigDecimal(5))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationForMultiCapture_capture2"))
                        .execute();
        assertTransactionResponse(capture2, TransactionStatus.Captured);

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal(7))
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditAuthorizationForMultiCapture_capture3"))
                        .execute();
        assertTransactionResponse(capture3, TransactionStatus.Captured);
    }

    @Test
    public void CreditVerify() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify"))
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    @Disabled
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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify_WithStoredCredentials"))
                        .execute();

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
                        .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify_With_Address"))
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
        assertNotNull(response.getTransactionId());
    }


    @Test
    public void VerifyTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.MULTIPLE)
                        .withClientTransactionId(ciTestingHarness.generateRandomId("verifyTokenizedPaymentMethod_tokenize"))
                        .execute()
                        .getToken();

        tokenizedCard.setToken(token);

        Transaction response =
                tokenizedCard
                        .verify()
                        .withCurrency("GBP")
                        .withClientTransactionId(ciTestingHarness.generateRandomId("verifyTokenizedPaymentMethod_verify"))
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals("VERIFIED", response.getResponseMessage());
    }

    @Test
    public void CreditVerify_Without_Currency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify_Without_Currency"))
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
    public void CreditVerify_InvalidCVV() throws ApiException {
        card.setCvn("1234");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify_InvalidCVV"))
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
    @Disabled
    public void CreditVerify_NotNumericCVV() throws ApiException {
        card.setCvn("SMA");
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditVerify_NotNumericCVV"))
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
    @Disabled
    public void CreditSale_ExpiryCard() throws ApiException {
        card.setExpYear(ciTestingHarness.getCurrentTime().getYear() - 1);

        boolean exceptionCaught = false;
        try {
            card
                    .charge(new BigDecimal(14))
                    .withCurrency("USD")
                    .withAllowDuplicates(true)
                    .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_ExpiryCard"))
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
    public void CreditSale_For_Android() throws ApiException {
        // GP-API settings for Android SDK
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setAndroid(true);

        final String GP_API_CONFIG_FOR_ANDROID_SDK = "GP_API_CONFIG_FOR_ANDROID_SDK";
        ServicesContainer.configureService(config, GP_API_CONFIG_FOR_ANDROID_SDK);

        Transaction response = card
                .charge(amount)
                .withCurrency(currency)
                .withClientTransactionId(ciTestingHarness.generateRandomId("creditSale_For_Android"))
                .execute(GP_API_CONFIG_FOR_ANDROID_SDK);

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    private static GpApiConfig gpApiSetup(String appId, String appKey, Channel channel) {
        GpApiConfig gpApiConfig = new GpApiConfig()
                .setAppId(appId)
                .setAppKey(appKey);

        gpApiConfig.setChannel(channel);

        gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        gpApiConfig.setEnableLogging(true);
        gpApiConfig.setRequestLogger(new RequestConsoleLogger());

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        accessTokenInfo.setRiskAssessmentAccountName("EOS_RiskAssessment");
        gpApiConfig.setAccessTokenInfo(accessTokenInfo);

        return gpApiConfig;
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

}
