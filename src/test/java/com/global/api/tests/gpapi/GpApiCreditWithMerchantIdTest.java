package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.services.Secure3dService;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GpApiCreditWithMerchantIdTest extends BaseGpApiTest {

    private CreditCardData card;
    private String merchantId;

    private final BigDecimal amount = new BigDecimal("24.02");
    private final String currency = "USD";
    private static String merchantConfig = null;

    @BeforeEach
    public void initialize() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID_FOR_MERCHANT, APP_KEY_FOR_MERCHANT, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");

        MerchantSummaryPaged merchants =
                ReportingService
                        .findMerchants(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.MerchantStatus, MerchantAccountStatus.ACTIVE)
                        .execute();

        assertTrue(merchants.getTotalRecordCount() > 0);
        assertTrue(merchants.getResults().size() <= 10);

        merchantId = merchants.getResults().get(0).getId();

        config.setMerchantId(merchantId);

        merchantConfig = "config_" + merchantId;

        MerchantAccountSummaryPaged response =
                ReportingService
                        .findAccounts(1, 10)
                        .orderBy(MerchantAccountsSortProperty.TIME_CREATED, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.MerchantId, merchantId)
                        .and(SearchCriteria.AccountStatus, MerchantAccountStatus.ACTIVE)
                        .and(SearchCriteria.PaymentMethodName, PaymentMethodName.Card)
                        .execute();

        List<MerchantAccountSummary> transactionAccounts = new ArrayList<>();

        for (MerchantAccountSummary account : response.getResults()) {
            if (account.getType() == MerchantAccountType.TRANSACTION_PROCESSING &&
                    account.getPaymentMethods().contains(PaymentMethodName.Card)) {
                transactionAccounts.add(account);
            }
        }

        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountID(transactionAccounts.get(0).getId());

        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config, merchantConfig);
    }

    @Test
    @Order(1)
    public void CreditAuthorization() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(amount.add(new BigDecimal("2")))
                        .withGratuity(new BigDecimal("2"))
                        .execute(merchantConfig);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    @Order(2)
    public void CreditAuthorization_CaptureLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(amount.subtract(new BigDecimal("2")))
                        .withGratuity(new BigDecimal("2"))
                        .execute(merchantConfig);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    @Order(3)
    public void CreditAuthorization_CaptureHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction capture =
                transaction
                        .capture(amount.multiply(new BigDecimal("1.15")))
                        .withGratuity(new BigDecimal("2"))
                        .execute(merchantConfig);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    @Order(4)
    public void CreditAuthorization_CaptureHigherAmount_WithError() throws ApiException {
        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .capture(amount.multiply(new BigDecimal("1.16")))
                    .withGratuity(new BigDecimal("2"))
                    .execute(merchantConfig);
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
    @Order(5)
    public void CreditSale() throws ApiException {
        Address address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setCountry("US");
        address.setPostalCode("12345");

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAddress(address)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(6)
    public void CreditSale_WithRequestMultiUseToken() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getToken());
    }

    @Test
    @Order(7)
    public void CreditRefund() throws ApiException {
        Transaction response =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(8)
    public void CreditRefundTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(9)
    public void CreditRefundTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(amount)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(merchantConfig);
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
    @Order(10)
    public void CreditRefundTransaction_RefundLowerAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .refund(amount.subtract(new BigDecimal("2")))
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(11)
    public void CreditRefundTransaction_RefundHigherAmount() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount.multiply(new BigDecimal("1.1")))
                    .withCurrency(currency)
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40087", ex.getResponseText());
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("Status Code: 400 - You may only refund up to 100% of the original amount ", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(12)
    public void CreditRefundTransactionWrongId() throws ApiException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TRN_" + UUID.randomUUID());
        transaction.setToken("TRN_" + UUID.randomUUID());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency(currency)
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40008", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Transaction " + transaction.getTransactionId() + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(13)
    public void CreditReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(amount)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(14)
    public void CreditReverseTransaction_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        Transaction response =
                transaction
                        .reverse(amount)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(merchantConfig);
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
    @Order(15)
    public void CreditReverseTransactionWrongId() throws ApiException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TRN_" + UUID.randomUUID());
        transaction.setToken("TRN_" + UUID.randomUUID());

        boolean exceptionCaught = false;
        try {
            transaction
                    .refund(amount)
                    .withCurrency(currency)
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + transaction.getTransactionId() + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(16)
    public void CreditPartialReverseTransaction() throws ApiException {
        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            transaction
                    .reverse(amount.subtract(new BigDecimal("1")))
                    .execute(merchantConfig);
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
    @Order(17)
    public void CreditAuthorizationForMultiCapture() throws ApiException {
        Transaction authorization =
                card
                        .authorize(new BigDecimal("14"))
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .execute(merchantConfig);

        assertNotNull(authorization);
        assertEquals(SUCCESS, authorization.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authorization.getResponseMessage());

        Transaction capture1 =
                authorization
                        .capture(new BigDecimal("3"))
                        .execute(merchantConfig);

        assertNotNull(capture1);
        assertEquals(SUCCESS, capture1.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture1.getResponseMessage());

        Transaction capture2 =
                authorization
                        .capture(new BigDecimal("5"))
                        .execute(merchantConfig);

        assertNotNull(capture2);
        assertEquals(SUCCESS, capture2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture2.getResponseMessage());

        Transaction capture3 =
                authorization
                        .capture(new BigDecimal("7"))
                        .execute(merchantConfig);

        assertNotNull(capture3);
        assertEquals(SUCCESS, capture3.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture3.getResponseMessage());
    }

    @Test
    @Order(18)
    public void CreditAuthorizationAndCapture_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction authorization =
                card
                        .authorize(new BigDecimal("14"))
                        .withCurrency(currency)
                        .withMultiCapture(true)
                        .execute(merchantConfig);

        assertNotNull(authorization);
        assertEquals(SUCCESS, authorization.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authorization.getResponseMessage());

        Transaction capture =
                authorization
                        .capture(new BigDecimal("14"))
                        .withIdempotencyKey(idempotencyKey)
                        .execute(merchantConfig);

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            authorization
                    .capture(new BigDecimal("14"))
                    .withIdempotencyKey(idempotencyKey)
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + capture.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(19)
    public void CreditCaptureWrongId() throws ApiException {
        Transaction authorization = new Transaction();
        authorization.setTransactionId("TRN_" + UUID.randomUUID());
        authorization.setToken("TRN_" + UUID.randomUUID());

        try {
            authorization
                    .capture(amount)
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + authorization.getTransactionId() + " not found at this location.", ex.getMessage());
        }
    }

    @Test
    @Order(20)
    public void SaleWithTokenizedPaymentMethod() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(merchantConfig));

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(21)
    public void CardTokenizationThenPayingWithToken_SingleToMultiUse() throws ApiException {
        String[] permissions = new String[]{"PMT_POST_Create_Single"};

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setPermissions(permissions);

        ServicesContainer.configureService(config, "singleUseToken");

        String token = card.tokenize(true, "singleUseToken", PaymentMethodUsageMode.SINGLE);

        assertNotNull(token);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);
        tokenizedCard.setCardHolderName("James Mason");

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withRequestMultiUseToken(true)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertTrue(response.getToken().startsWith("PMT_"));

        tokenizedCard.setToken(response.getToken());

        tokenizedCard
                .charge(10)
                .withCurrency(currency)
                .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(22)
    public void CreditVerify() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    @Order(23)
    public void CreditVerify_WithAddress() throws ApiException {
        Address address = new Address();
        address.setPostalCode("750241234");
        address.setStreetAddress1("6860 Dallas Pkwy");

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withAddress(address)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    @Order(24)
    public void CreditVerify_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(merchantConfig);
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
    @Order(25)
    public void CreditVerify_WithoutCurrency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            card
                    .verify()
                    .execute(merchantConfig);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("MANDATORY_DATA_MISSING", ex.getResponseCode());
            assertEquals("40005", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields currency", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @Order(26)
    public void CreditChargeTransactions_WithSameIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction transaction =
                card
                        .charge(4.95)
                        .withCurrency(currency)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(merchantConfig);

        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        boolean exceptionCaught = false;
        try {
            card
                    .charge(4.95)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(merchantConfig);
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
    @Order(27)
    public void CreditVerify_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
    }

    @Test
    @Order(28)
    public void CreditSale_WithStoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    @Order(29)
    public void CreditSale_WithStoredCredentials_RecurringPayment() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(merchantConfig));

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .execute(merchantConfig);

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
                        .execute(merchantConfig);

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
                        .execute(merchantConfig);

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
                        .execute(merchantConfig);

        assertNotNull(response2);
        assertEquals(SUCCESS, response2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response2.getResponseMessage());
    }

    @Test
    @Order(30)
    public void CreditSale_WithCardBrandStorage_RecurringPayment() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(merchantConfig));

        Transaction response =
                tokenizedCard
                        .charge(10.01)
                        .withCurrency("GBP")
                        .execute(merchantConfig);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());

        Transaction response2 =
                tokenizedCard
                        .charge(10.01)
                        .withCurrency("GBP")
                        .withStoredCredential(
                                new StoredCredential()
                                        .setInitiator(StoredCredentialInitiator.Merchant)
                                        .setType(StoredCredentialType.Recurring)
                                        .setSequence(StoredCredentialSequence.Subsequent)
                                        .setReason(StoredCredentialReason.Incremental))
                        .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                        .execute(merchantConfig);

        assertNotNull(response2);
        assertEquals(SUCCESS, response2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response2.getResponseMessage());
        assertNotNull(response2.getCardBrandTransactionId());
    }

    @AfterAll
    public static void removeConfig() throws ConfigurationException {
        ServicesContainer.removeConfig(merchantConfig);
    }
}
