package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.PayByLinkSummary;
import com.global.api.entities.reporting.PayByLinkSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.PayByLinkService;
import com.global.api.services.Secure3dService;
import com.global.api.utils.GenerationUtils;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARD_AUTH_SUCCESSFUL_V2_1;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiPayByLinkTest extends BaseGpApiTest {

    private final CreditCardData card;
    private PayByLinkData payByLink;
    private final Address shippingAddress;
    private final BrowserData browserData;
    private BigDecimal amount = new BigDecimal("2.11");
    private final String currency = "GBP";
    private String payByLinkId = null;

    public GpApiPayByLinkTest() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("v2yRaFOLwFaQc0fSZTCyAdQCBNByGpVK")
                .setAppKey("oKZpWitk6tORoCVT")
                .setChannel(Channel.CardNotPresent);

        config.setEnvironment(Environment.TEST);
        config.setCountry("GB");
        AccessTokenInfo accessTokenInfo =
                new AccessTokenInfo()
                        .setTransactionProcessingAccountName("LinkManagement");

        config.setAccessTokenInfo(accessTokenInfo);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        payByLink = new PayByLinkData();
        payByLink.setType(PayByLinkType.PAYMENT);
        payByLink.setUsageMode(PaymentMethodUsageMode.SINGLE);
        payByLink.setAllowedPaymentMethods(new String[]{PaymentMethodName.Card.getValue(Target.GP_API)});
        payByLink.setUsageLimit(3);
        payByLink.setName("Mobile Bill Payment");
        payByLink.isShippable(true);
        payByLink.setShippingAmount(new BigDecimal("1.23"));
        payByLink.setExpirationDate(DateTime.now().plusDays(10));
        payByLink.setImages(new ArrayList<>());
        payByLink.setReturnUrl("https://www.example.com/returnUrl");
        payByLink.setStatusUpdateUrl("https://www.example.com/statusUrl");
        payByLink.setCancelUrl("https://www.example.com/returnUrl");

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);

        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Chicago");
        shippingAddress.setPostalCode("5001");
        shippingAddress.setState("IL");
        shippingAddress.setCountryCode("840");

        browserData = new BrowserData();
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

        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 1)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayByLinkStatus, PayByLinkStatus.ACTIVE.toString())
                        .execute();

        if (response.getResults().size() >= 1) {
            payByLinkId = response.getResults().get(0).getId();
        }
    }

    @Test
    public void ReportPayByLinkDetail() throws ApiException {
        String payByLinkId = "LNK_XXmoF2d4UVBs4k8oXwuqPw1LFQCvc2";

        PayByLinkSummary response =
                PayByLinkService
                        .payByLinkDetail(payByLinkId)
                        .execute();

        assertNotNull(response);
        assertEquals(payByLinkId, response.getId());
    }

    @Test
    public void ReportPayByLinkDetail_RandomId() throws ApiException {
        String payByLinkId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .payByLinkDetail(payByLinkId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Links " + payByLinkId + " not found at this /ucp/links/" + payByLinkId + "", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportPayByLinkDetail_NullLinkId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .payByLinkDetail(null)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("payByLinkId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FindPayByLinkByDate() throws ApiException {
        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get(0);
        assertNotNull(randomPayByLink);
    }

    @Test
    public void FindPayByLinkByDate_NoResults() throws ApiException {
        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, DateTime.now().minusMonths(24).toDate())
                        .and(SearchCriteria.EndDate, DateTime.now().minusMonths(22).toDate())
                        .execute();

        assertNotNull(response);
        assertEquals(0, response.getResults().size());
        assertEquals(0, response.getTotalRecordCount());
    }

    @Test
    public void CreatePayByLink() throws ApiException {
        PayByLinkData payByLink = new PayByLinkData();

        payByLink.setType(PayByLinkType.PAYMENT);
        payByLink.setUsageMode(PaymentMethodUsageMode.SINGLE);
        payByLink.setAllowedPaymentMethods(new String[]{PaymentMethodName.Card.getValue(Target.GP_API)});
        payByLink.setUsageLimit(1);
        payByLink.setName("Mobile Bill Payment");
        payByLink.isShippable(true);
        payByLink.setShippingAmount(new BigDecimal("1.23"));
        payByLink.setExpirationDate(DateTime.now().plusDays(10));
        payByLink.setImages(new ArrayList<>());
        payByLink.setReturnUrl("https://www.example.com/returnUrl");
        payByLink.setStatusUpdateUrl("https://www.example.com/statusUrl");
        payByLink.setCancelUrl("https://www.example.com/returnUrl");

        BigDecimal amount = new BigDecimal("10.01");
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency("GBP")
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(PayByLinkStatus.ACTIVE.toString(), response.getResponseMessage());
        assertEquals(amount, response.getBalanceAmount());
        assertNotNull(response.getPayByLinkResponse().getUrl());
        assertNotNull(response.getPayByLinkResponse().getId());
        assertTrue(response.getPayByLinkResponse().getIsShippable());
    }

    @Test
    public void CreatePayByLink_MultipleUsage() throws ApiException {
        payByLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payByLink.setUsageLimit(2);

        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);
    }

    @Test
    public void CreatePayByLink_ThenCharge() throws ApiException, InterruptedException {
        List<String> imagesList = new ArrayList<>();
        imagesList.add("\"https://gpapi-sandbox.truust.io/assets/images/37272.jpg\"");
        imagesList.add("\"https://gpapi-sandbox.truust.io/assets/images/37272.jpg\"");

        payByLink.setImages(imagesList);

        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
    }

    @Test
    public void CreatePayByLink_ThenCharge_DifferentAmount() throws ApiException, InterruptedException {
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction chargeTransaction =
                card
                        .charge(amount.add(BigDecimal.valueOf(2)))
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
        assertEquals(amount.add(BigDecimal.valueOf(2)), getPayByLinkById.getTransactions().get(0).getAmount());
    }

    @Test
    public void CreatePayByLink_MultipleUsage_ThenCharge() throws ApiException, InterruptedException {
        payByLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payByLink.setUsageLimit(2);

        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        for (int i = 1; i <= payByLink.getUsageLimit(); i++) {
            Transaction chargeTransaction =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .withPaymentLinkId(response.getPayByLinkResponse().getId())
                            .execute("createTransaction");

            assertNotNull(chargeTransaction);
            assertEquals(SUCCESS, chargeTransaction.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());
        }

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
    }

    @Test
    public void CreatePayByLink_ThenAuthorizeAndCapture() throws ApiException, InterruptedException {
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction authTransaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(authTransaction);
        assertEquals(SUCCESS, authTransaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authTransaction.getResponseMessage());

        Transaction capture =
                authTransaction
                        .capture(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
    }

    @Test
    public void CreatePayByLink_ThenCharge_WithTokenizedCard() throws ApiException, InterruptedException {
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        String token =
                card
                        .tokenize(true, PaymentMethodUsageMode.SINGLE)
                        .execute("createTransaction")
                        .getToken();

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction chargeTransaction =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
    }

    @Test
    public void CreatePayByLink_ThenCharge_With3DS() throws ApiException, InterruptedException {
        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute("createTransaction");

        assertNotNull(secureEcom);
        assertEquals("ENROLLED", secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());

        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute("createTransaction");

        assertNotNull(initAuth);
        assertEquals("SUCCESS_AUTHENTICATED", initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute("createTransaction");

        assertEquals("SUCCESS_AUTHENTICATED", secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayByLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayByLinkSummary getPayByLinkById =
                PayByLinkService
                        .payByLinkDetail(response.getPayByLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayByLinkResponse().getId(), getPayByLinkById.getId());
    }

    @Test
    public void EditPayByLink() throws ApiException {
        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayByLinkStatus, PayByLinkStatus.ACTIVE.toString())
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get(0);
        assertNotNull(randomPayByLink);
        assertNotNull(randomPayByLink.getId());

        PayByLinkData payByLink = new PayByLinkData();

        payByLink.setName("Test of Test");
        payByLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payByLink.setType(PayByLinkType.PAYMENT);
        payByLink.setUsageLimit(5);
        payByLink.isShippable(false);
        BigDecimal newAmount = new BigDecimal("10.08");

        Transaction editResponse =
                PayByLinkService
                        .edit(randomPayByLink.getId())
                        .withAmount(newAmount)
                        .withPayByLinkData(payByLink)
                        .withDescription("Update PayByLink description")
                        .execute();

        assertEquals("SUCCESS", editResponse.getResponseCode());
        assertEquals(PayByLinkStatus.ACTIVE.toString(), editResponse.getResponseMessage());
        assertEquals(newAmount, editResponse.getBalanceAmount());
        assertNotNull(editResponse.getPayByLinkResponse().getUrl());
        assertNotNull(editResponse.getPayByLinkResponse().getId());
    }

    @Test
    public void CreatePayByLink_MissingUsageMode() throws ApiException {
        payByLink.setUsageMode(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withCurrency(currency)
                    .withDescription("March and April Invoice")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field usage_mode", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreatePayByLink_MissingPaymentMethods() throws ApiException {
        payByLink.setAllowedPaymentMethods(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withCurrency(currency)
                    .withDescription("March and April Invoice")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field transactions.allowed_payment_methods", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreatePayByLink_MissingName() throws ApiException {
        payByLink.setName(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withCurrency(currency)
                    .withDescription("March and April Invoice")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field name", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreatePayByLink_MissingShippable() throws ApiException {
        payByLink.isShippable(null);

        Transaction response =
                PayByLinkService
                        .create(payByLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayByLinkResponse(response);
        assertFalse(response.getPayByLinkResponse().getIsShippable());
    }

    @Test
    public void CreatePayByLink_MissingShippingAmount() throws ApiException {
        payByLink.setShippingAmount(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withCurrency(currency)
                    .withDescription("March and April Invoice")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40251", e.getResponseText());
            assertEquals("Status Code: 422 - Request expects the following fields: shipping_amount.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreatePayByLink_MissingDescription() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field description", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreatePayByLink_MissingCurrency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .create(payByLink, amount)
                    .withDescription("March and April Invoice")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field transactions.currency", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingType() throws ApiException {
        assertNotNull(payByLinkId);

        payByLink.setType(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("type cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingUsageMode() throws ApiException {
        payByLink.setUsageMode(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("usageMode cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingName() throws ApiException {
        assertNotNull(payByLinkId);

        payByLink.setName(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field name", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    //TODO - usageLimit is incorrect set as optional
    @Test
    public void EditPayByLink_MissingUsageLimit() throws ApiException {
        assertNotNull(payByLinkId);

        payByLink.setUsageLimit(null);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("usageLimit cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingDescription() throws ApiException {
        assertNotNull(payByLinkId);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following field description", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingAmount() throws ApiException {
        assertNotNull(payByLinkId);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(null)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_MissingPayByLinkData() throws ApiException {
        assertNotNull(payByLinkId);

        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(payByLinkId)
                    .withAmount(amount)
                    .withPayByLinkData(null)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("payByLinkData cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayByLink_RandomPayByLinkId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayByLinkService
                    .edit(UUID.randomUUID().toString())
                    .withAmount(amount)
                    .withPayByLinkData(payByLink)
                    .withDescription("Update PayByLink description")
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40108", e.getResponseText());
            assertEquals("Status Code: 400 - You cannot update a link that has a 400 status", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FindPayByLinkByStatus() throws ApiException {
        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayByLinkStatus, PayByLinkStatus.EXPIRED.toString())
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayByLink);
        assertEquals(PayByLinkStatus.EXPIRED, randomPayByLink.getStatus());
    }

    @Test
    public void FindPayByLinkUsageModeAndName() throws ApiException {
        String name = "iphone 14";

        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PaymentMethodUsageMode, PaymentMethodUsageMode.SINGLE.toString())
                        .and(SearchCriteria.DisplayName, name)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayByLink);
        assertEquals(PaymentMethodUsageMode.SINGLE, randomPayByLink.getUsageMode());
        assertEquals(name, randomPayByLink.getName());
    }

    @Test
    public void FindPayByLinkByAmount() throws ApiException {
        amount = new BigDecimal("10.01");

        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayByLink);
        assertEquals(amount, randomPayByLink.getAmount());
    }

    @Test
    public void FindPayByLinkByExpireDate() throws ApiException {
        DateTime date = new DateTime("2024-05-09");

        PayByLinkSummaryPaged response =
                PayByLinkService
                        .findPayByLink(1, 10)
                        .orderBy(PayByLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.ExpirationDate, date)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayByLinkSummary randomPayByLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayByLink);
        assertEquals(GpApiConnector.parseGpApiDate(date.toString()), GpApiConnector.parseGpApiDate(randomPayByLink.getExpirationDate().toString()));
    }

    private void assertPayByLinkResponse(Transaction response) {
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(PayByLinkStatus.ACTIVE.toString(), response.getResponseMessage());
        assertEquals(amount, response.getBalanceAmount());
        assertNotNull(response.getPayByLinkResponse().getUrl());
        assertNotNull(response.getPayByLinkResponse().getId());
    }

    private GpApiConfig setupTransactionConfig() {
        GpApiConfig config = new GpApiConfig();

        config.setAppId(APP_ID);
        config.setAppKey(APP_KEY);
        config.setChannel(Channel.CardNotPresent);
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");
        config.setEnableLogging(true);

        return config;
    }
}