package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.DataServiceCriteria;
import com.global.api.entities.reporting.PayLinkSummary;
import com.global.api.entities.reporting.PayLinkSummaryPaged;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.gateways.GpApiConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.PayLinkService;
import com.global.api.services.Secure3dService;
import com.global.api.utils.GenerationUtils;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARD_AUTH_SUCCESSFUL_V2_1;
import static org.junit.Assert.*;

public class GpApiPayLinkTest extends BaseGpApiTest {

    private final CreditCardData card;
    private PayLinkData payLink;
    private final Address shippingAddress;
    private final BrowserData browserData;
    private BigDecimal amount = new BigDecimal("2.11");
    private final String currency = "GBP";
    private String payLinkId = null;

    public GpApiPayLinkTest() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("v2yRaFOLwFaQc0fSZTCyAdQCBNByGpVK")
                .setAppKey("oKZpWitk6tORoCVT")
                .setChannel(Channel.CardNotPresent.getValue());

        config.setEnvironment(Environment.TEST);
        config.setCountry("GB");
        AccessTokenInfo accessTokenInfo =
                new AccessTokenInfo()
                        .setTransactionProcessingAccountName("LinkManagement");

        config.setAccessTokenInfo(accessTokenInfo);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        payLink = new PayLinkData();
        payLink.setType(PayLinkType.PAYMENT);
        payLink.setUsageMode(PaymentMethodUsageMode.SINGLE);
        payLink.setAllowedPaymentMethods(new String[]{PaymentMethodName.Card.getValue(Target.GP_API)});
        payLink.setUsageLimit(3);
        payLink.setName("Mobile Bill Payment");
        payLink.isShippable(true);
        payLink.setShippingAmount(new BigDecimal("1.23"));
        payLink.setExpirationDate(DateTime.now().plusDays(10));
        payLink.setImages(new ArrayList<>());
        payLink.setReturnUrl("https://www.example.com/returnUrl");
        payLink.setStatusUpdateUrl("https://www.example.com/statusUrl");
        payLink.setCancelUrl("https://www.example.com/returnUrl");

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

        PayLinkSummaryPaged response =
                PayLinkService
                        .findPayLink(1, 1)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayLinkStatus, PayLinkStatus.ACTIVE.toString())
                        .execute();

        if (response.getResults().size() >= 1) {
            payLinkId = response.getResults().get(0).getId();
        }
    }

    @Test
    public void ReportPayLinkDetail() throws ApiException {
        var paylinkId = "LNK_XXmoF2d4UVBs4k8oXwuqPw1LFQCvc2";

        var response =
                PayLinkService
                        .payLinkDetail(paylinkId)
                        .execute();

        assertNotNull(response);
        assertEquals(paylinkId, response.getId());
    }

    @Test
    public void ReportPayLinkDetail_RandomId() throws ApiException {
        var paylinkId = UUID.randomUUID().toString();

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .payLinkDetail(paylinkId)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40118", ex.getResponseText());
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("Status Code: 404 - Links " + paylinkId + " not found at this /ucp/links/" + paylinkId + "", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void ReportPayLinkDetail_NullLinkId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .payLinkDetail(null)
                    .execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("payLinkId cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FindPayLinkByDate() throws ApiException {
        var response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        var randomPayLink = response.getResults().get(0);
        assertNotNull(randomPayLink);
    }

    @Test
    public void FindPayLinkByDate_NoResults() throws ApiException {
        var response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, DateTime.now().minusMonths(24).toDate())
                        .and(SearchCriteria.EndDate, DateTime.now().minusMonths(22).toDate())
                        .execute();

        assertNotNull(response);
        assertEquals(0, response.getResults().size());
        assertEquals(0, response.getTotalRecordCount());
    }

    @Test
    public void CreatePayLink() throws ApiException {
        var payLink = new PayLinkData();

        payLink.setType(PayLinkType.PAYMENT);
        payLink.setUsageMode(PaymentMethodUsageMode.SINGLE);
        payLink.setAllowedPaymentMethods(new String[]{PaymentMethodName.Card.getValue(Target.GP_API)});
        payLink.setUsageLimit(1);
        payLink.setName("Mobile Bill Payment");
        payLink.isShippable(true);
        payLink.setShippingAmount(new BigDecimal("1.23"));
        payLink.setExpirationDate(DateTime.now().plusDays(10));
        payLink.setImages(new ArrayList<>());
        payLink.setReturnUrl("https://www.example.com/returnUrl");
        payLink.setStatusUpdateUrl("https://www.example.com/statusUrl");
        payLink.setCancelUrl("https://www.example.com/returnUrl");

        BigDecimal amount = new BigDecimal("10.01");
        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency("GBP")
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(PayLinkStatus.ACTIVE.toString(), response.getResponseMessage());
        assertEquals(amount, response.getBalanceAmount());
        assertNotNull(response.getPayLinkResponse().getUrl());
        assertNotNull(response.getPayLinkResponse().getId());
        assertTrue(response.getPayLinkResponse().getIsShippable());
    }

    @Test
    public void CreatePayLink_MultipleUsage() throws ApiException {
        payLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payLink.setUsageLimit(2);

        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);
    }

    @Test
    public void CreatePayLink_ThenCharge() throws ApiException, InterruptedException {
        List<String> imagesList = new ArrayList<>();
        imagesList.add("\"https://gpapi-sandbox.truust.io/assets/images/37272.jpg\"");
        imagesList.add("\"https://gpapi-sandbox.truust.io/assets/images/37272.jpg\"");

        payLink.setImages(imagesList);

        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
    }

    @Test
    public void CreatePayLink_ThenCharge_DifferentAmount() throws ApiException, InterruptedException {
        Transaction response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction chargeTransaction =
                card
                        .charge(amount.add(BigDecimal.valueOf(2)))
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
        assertEquals(amount.add(BigDecimal.valueOf(2)), getPayLinkById.getTransactions().get(0).getAmount());
    }

    @Test
    public void CreatePayLink_MultipleUsage_ThenCharge() throws ApiException, InterruptedException {
        payLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payLink.setUsageLimit(2);

        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        for (int i = 1; i <= payLink.getUsageLimit(); i++) {
            Transaction chargeTransaction =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .withPaymentLinkId(response.getPayLinkResponse().getId())
                            .execute("createTransaction");

            assertNotNull(chargeTransaction);
            assertEquals(SUCCESS, chargeTransaction.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());
        }

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
    }

    @Test
    public void CreatePayLink_ThenAuthorizeAndCapture() throws ApiException, InterruptedException {
        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

        ServicesContainer.configureService(setupTransactionConfig(), "createTransaction");

        Transaction authTransaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(authTransaction);
        assertEquals(SUCCESS, authTransaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), authTransaction.getResponseMessage());

        Transaction capture =
                authTransaction
                        .capture(amount)
                        .withCurrency(currency)
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(capture);
        assertEquals(SUCCESS, capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
    }

    @Test
    public void CreatePayLink_ThenCharge_WithTokenizedCard() throws ApiException, InterruptedException {
        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

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
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
    }

    @Test
    public void CreatePayLink_ThenCharge_With3DS() throws ApiException, InterruptedException {
        var response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);

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
                        .withPaymentLinkId(response.getPayLinkResponse().getId())
                        .execute("createTransaction");

        assertNotNull(chargeTransaction);
        assertEquals(SUCCESS, chargeTransaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), chargeTransaction.getResponseMessage());

        Thread.sleep(2000);

        PayLinkSummary getPayLinkById =
                PayLinkService
                        .payLinkDetail(response.getPayLinkResponse().getId())
                        .execute();

        assertNotNull(response);
        assertEquals(response.getPayLinkResponse().getId(), getPayLinkById.getId());
    }

    @Test
    public void EditPayLink() throws ApiException {
        var response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayLinkStatus, PayLinkStatus.ACTIVE.toString())
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        var randomPayLink = response.getResults().get(0);
        assertNotNull(randomPayLink);
        assertNotNull(randomPayLink.getId());

        var payLink = new PayLinkData();

        payLink.setName("Test of Test");
        payLink.setUsageMode(PaymentMethodUsageMode.MULTIPLE);
        payLink.setType(PayLinkType.PAYMENT);
        payLink.setUsageLimit(5);
        payLink.isShippable(false);
        var newAmount = new BigDecimal("10.08");

        var editResponse =
                PayLinkService
                        .edit(randomPayLink.getId())
                        .withAmount(newAmount)
                        .withPayLinkData(payLink)
                        .withDescription("Update Paylink description")
                        .execute();

        assertEquals("SUCCESS", editResponse.getResponseCode());
        assertEquals(PayLinkStatus.ACTIVE.toString(), editResponse.getResponseMessage());
        assertEquals(newAmount, editResponse.getBalanceAmount());
        assertNotNull(editResponse.getPayLinkResponse().getUrl());
        assertNotNull(editResponse.getPayLinkResponse().getId());
    }

    @Test
    public void CreatePayLink_MissingUsageMode() throws ApiException {
        payLink.setUsageMode(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void CreatePayLink_MissingPaymentMethods() throws ApiException {
        payLink.setAllowedPaymentMethods(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void CreatePayLink_MissingName() throws ApiException {
        payLink.setName(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void CreatePayLink_MissingShippable() throws ApiException {
        payLink.isShippable(null);

        Transaction response =
                PayLinkService
                        .create(payLink, amount)
                        .withCurrency(currency)
                        .withClientTransactionId(GenerationUtils.generateRecurringKey())
                        .withDescription("March and April Invoice")
                        .execute();

        assertPayLinkResponse(response);
        assertFalse(response.getPayLinkResponse().getIsShippable());
    }

    @Test
    public void CreatePayLink_MissingShippingAmount() throws ApiException {
        payLink.setShippingAmount(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void CreatePayLink_MissingDescription() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void CreatePayLink_MissingCurrency() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .create(payLink, amount)
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
    public void EditPayLink_MissingType() throws ApiException {
        assertNotNull(payLinkId);

        payLink.setType(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("type cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayLink_MissingUsageMode() throws ApiException {
        payLink.setUsageMode(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("usageMode cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayLink_MissingName() throws ApiException {
        assertNotNull(payLinkId);

        payLink.setName(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
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
    public void EditPayLink_MissingUsageLimit() throws ApiException {
        assertNotNull(payLinkId);

        payLink.setUsageLimit(null);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("usageLimit cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayLink_MissingDescription() throws ApiException {
        assertNotNull(payLinkId);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(payLink)
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
    public void EditPayLink_MissingAmount() throws ApiException {
        assertNotNull(payLinkId);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(null)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayLink_MissingPayLinkData() throws ApiException {
        assertNotNull(payLinkId);

        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(payLinkId)
                    .withAmount(amount)
                    .withPayLinkData(null)
                    .withDescription("Update Paylink description")
                    .execute();
        } catch (BuilderException e) {
            exceptionCaught = true;
            assertEquals("payLinkData cannot be null for this transaction type.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void EditPayLink_RandomPayLinkId() throws ApiException {
        boolean exceptionCaught = false;
        try {
            PayLinkService
                    .edit(UUID.randomUUID().toString())
                    .withAmount(amount)
                    .withPayLinkData(payLink)
                    .withDescription("Update Paylink description")
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
    public void FindPayLinkByStatus() throws ApiException {
        PayLinkSummaryPaged response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PayLinkStatus, PayLinkStatus.EXPIRED.toString())
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayLinkSummary randomPayLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayLink);
        assertEquals(PayLinkStatus.EXPIRED, randomPayLink.getStatus());
    }

    @Test
    public void FindPayLinkUsageModeAndName() throws ApiException {
        String name = "iphone 14";

        PayLinkSummaryPaged response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.PaymentMethodUsageMode, PaymentMethodUsageMode.SINGLE.toString())
                        .and(SearchCriteria.DisplayName, name)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayLinkSummary randomPayLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayLink);
        assertEquals(PaymentMethodUsageMode.SINGLE, randomPayLink.getUsageMode());
        assertEquals(name, randomPayLink.getName());
    }

    @Test
    public void FindPayLinkByAmount() throws ApiException {
        amount = new BigDecimal("10.01");

        PayLinkSummaryPaged response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(DataServiceCriteria.Amount, amount)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayLinkSummary randomPayLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayLink);
        assertEquals(amount, randomPayLink.getAmount());
    }

    @Test
    public void FindPayLinkByExpireDate() throws ApiException {
        DateTime date = new DateTime("2024-05-09");

        PayLinkSummaryPaged response =
                PayLinkService
                        .findPayLink(1, 10)
                        .orderBy(PayLinkSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.ExpirationDate, date)
                        .execute();

        assertNotNull(response);
        assertNotNull(response.getResults());
        PayLinkSummary randomPayLink = response.getResults().get((new Random()).ints(0, response.getResults().size()).findFirst().getAsInt());
        assertNotNull(randomPayLink);
        assertEquals(GpApiConnector.parseGpApiDate(date.toString()), GpApiConnector.parseGpApiDate(randomPayLink.getExpirationDate().toString()));
    }

    private void assertPayLinkResponse(Transaction response) {
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(PayLinkStatus.ACTIVE.toString(), response.getResponseMessage());
        assertEquals(amount, response.getBalanceAmount());
        assertNotNull(response.getPayLinkResponse().getUrl());
        assertNotNull(response.getPayLinkResponse().getId());
    }

    private GpApiConfig setupTransactionConfig() {
        GpApiConfig config = new GpApiConfig();

        config.setAppId(APP_ID);
        config.setAppKey(APP_KEY);
        config.setChannel(Channel.CardNotPresent.getValue());
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");
        config.setEnableLogging(true);

        return config;
    }
}