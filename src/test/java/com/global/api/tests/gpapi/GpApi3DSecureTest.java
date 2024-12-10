package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.gateways.SSLSocketFactoryEx;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import com.global.api.utils.IOUtils;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApi3DSecureTest extends BaseGpApiTest {

    private final static String AVAILABLE = "AVAILABLE";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String ENROLLED = "ENROLLED";
    private final static String SUCCESS_AUTHENTICATED = "SUCCESS_AUTHENTICATED";
    private static final BigDecimal amount = new BigDecimal("10.01");
    private static final String currency = "GBP";
    private final CreditCardData card;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BrowserData browserData;
    private final StoredCredential storedCredential;
    private final MobileData mobileData;

    public GpApi3DSecureTest() throws ConfigurationException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

        // Create card data
        card = new CreditCardData();
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCardHolderName("John Smith");
        card.setCardPresent(true);

        // Stored card
        RecurringPaymentMethod stored = new RecurringPaymentMethod("20190809-Realex", "20190809-Realex-Credit");

        // Shipping address
        shippingAddress = new Address();

        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Chicago");
        shippingAddress.setPostalCode("5001");
        shippingAddress.setState("IL");
        shippingAddress.setCountryCode("840");

        // Billing address
        billingAddress = new Address();

        billingAddress.setStreetAddress1("Flat 456");
        billingAddress.setStreetAddress2("House 789");
        billingAddress.setStreetAddress3("no");
        billingAddress.setCity("Halifax");
        billingAddress.setPostalCode("W5 9HR");
        billingAddress.setCountryCode("826");

        // Browser data
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

        storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Recurring);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        // Mobile data
        mobileData =
                new MobileData()
                        .setEncodedData("ew0KCSJEViI6ICIxLjAiLA0KCSJERCI6IHsNCgkJIkMwMDEiOiAiQW5kcm9pZCIsDQoJCSJDMDAyIjogIkhUQyBPbmVfTTgiLA0KCQkiQzAwNCI6ICI1LjAuMSIsDQoJCSJDMDA1IjogImVuX1VTIiwNCgkJIkMwMDYiOiAiRWFzdGVybiBTdGFuZGFyZCBUaW1lIiwNCgkJIkMwMDciOiAiMDY3OTc5MDMtZmI2MS00MWVkLTk0YzItNGQyYjc0ZTI3ZDE4IiwNCgkJIkMwMDkiOiAiSm9obidzIEFuZHJvaWQgRGV2aWNlIg0KCX0sDQoJIkRQTkEiOiB7DQoJCSJDMDEwIjogIlJFMDEiLA0KCQkiQzAxMSI6ICJSRTAzIg0KCX0sDQoJIlNXIjogWyJTVzAxIiwgIlNXMDQiXQ0KfQ0K")
                        .setApplicationReference("f283b3ec-27da-42a1-acea-f3f70e75bbdc")
                        .setSdkInterface(SdkInterface.Both)
                        .setSdkUiTypes(SdkUiType.OOB)
                        .setEphemeralPublicKey(
                                JsonDoc.parse("{" +
                                        "\"kty\":\"EC\"," +
                                        "\"crv\":\"P-256\"," +
                                        "\"x\":\"WWcpTjbOqiu_1aODllw5rYTq5oLXE_T0huCPjMIRbkI\",\"y\":\"Wz_7anIeadV8SJZUfr4drwjzuWoUbOsHp5GdRZBAAiw\"" +
                                        "}"
                                )
                        )
                        .setMaximumTimeout(50)
                        .setReferenceNumber("3DS_LOA_SDK_PPFU_020100_00007")
                        .setSdkTransReference("b2385523-a66c-4907-ac3c-91848e8c0067");
    }

    @Test
    public void FullCycle_v2() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        storedCredential.setType(StoredCredentialType.Unscheduled);
        storedCredential.setSequence(StoredCredentialSequence.First);

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withCustomerEmail("jason@globalpay.com")
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_WithTokenizedPaymentMethod() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_WithPayerInformation() throws ApiException {
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertNull(secureEcom.getEci());
        assertTrue(secureEcom.isEnrolled());

        ThreeDSecure initAuth = Secure3dService
                .initiateAuthentication(tokenizedCard, secureEcom)
                .withAmount(amount)
                .withCurrency(currency)
                .withAuthenticationSource(AuthenticationSource.Browser)
                .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                .withOrderCreateDate(DateTime.now())
                .withAddress(billingAddress, AddressType.Billing)
                .withAddress(shippingAddress, AddressType.Shipping)
                .withBrowserData(browserData)
                .withCustomerAccountId("6dcb24f5-74a0-4da3-98da-4f0aa0e88db3")
                .withAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                .withAccountCreateDate(DateTime.now().plusYears(-2))
                .withAccountChangeDate(DateTime.now().plusYears(-2))
                .withAccountChangeIndicator(AgeIndicator.LessThanThirtyDays)
                .withPasswordChangeDate(DateTime.now())
                .withPasswordChangeIndicator(AgeIndicator.LessThanThirtyDays)
                .withHomeNumber("44", "123456798")
                .withWorkNumber("44", "1801555888")
                .withMobileNumber("44", "7975556677")
                .withPaymentAccountCreateDate(DateTime.now())
                .withPaymentAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                .withSuspiciousAccountActivity(SuspiciousAccountActivity.SUSPICIOUS_ACTIVITY)
                .withNumberOfPurchasesInLastSixMonths(3)
                .withNumberOfTransactionsInLast24Hours(1)
                .withNumberOfTransactionsInLastYear(5)
                .withNumberOfAddCardAttemptsInLast24Hours(1)
                .withShippingAddressCreateDate(DateTime.now().plusYears(-2))
                .withShippingAddressUsageIndicator(AgeIndicator.ThisTransaction)
                .withCustomerEmail("james@globalpay.com")
                .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        ThreeDSecure secureEcom2 = Secure3dService.getAuthenticationData()
                .withServerTransactionId(secureEcom.getServerTransactionId())
                .execute();
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom2.getStatus());
        assertEquals("YES", secureEcom2.getLiabilityShift());
    }

    @Test
    public void FullCycle_v2_CreditSale_WithStoredCredentials_RecurringPayment() throws ApiException {
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertNull(secureEcom.getEci());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        assertNotNull(response.getCardBrandTransactionId());

        Transaction response2 =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                        .execute();

        assertNotNull(response2);
        assertEquals(SUCCESS, response2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response2.getResponseMessage());
    }

    @Test
    public void testChargeTransaction_WithRandom3DSValues() throws ApiException {
        card.setNumber(CARD_CHALLENGE_REQUIRED_V2_1.cardNumber);

        ThreeDSecure threeDS = new ThreeDSecure();
        threeDS.setAuthenticationValue(UUID.randomUUID().toString());
        threeDS.setDirectoryServerTransactionId(UUID.randomUUID().toString());
        threeDS.setEci(UUID.randomUUID().toString());
        threeDS.setMessageVersion(UUID.randomUUID().toString());

        card.setThreeDSecure(threeDS);

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_WithMobileSdk() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_2.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .withMobileData(mobileData)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());
        assertNotNull(initAuth.getProviderServerTransRef());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_v2() throws Exception {
        // Challenge required scenario
        card.setNumber(CARD_CHALLENGE_REQUIRED_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(CHALLENGE_REQUIRED, initAuth.getStatus());
        assertTrue(initAuth.isChallengeMandated());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertTrue(secureEcom.isChallengeMandated());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(initAuth.getIssuerAcsUrl());
        String authResponse = acsClient.authenticate_v2(initAuth);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void Frictionless_AuthenticationSuccessful_FullCycle_v2_DifferentAmount() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_2.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertEquals(amount, secureEcom.getAmount());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("15"))
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(Secure3dVersion.TWO, initAuth.getVersion());
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertNotNull(initAuth.getChallengeReturnUrl());
        assertNotNull(initAuth.getSessionDataFieldName());
        assertNotNull(initAuth.getMessageType());
        assertEquals(amount, initAuth.getAmount());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());
        assertEquals("05", secureEcom.getEci());
        assertEquals(amount, secureEcom.getAmount());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_ForceFrictionlessToDoChallenge() throws Exception {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
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
                        .withChallengeRequestIndicator(ChallengeRequestIndicator.ChallengeMandated)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(CHALLENGE_REQUIRED, initAuth.getStatus());
        assertTrue(initAuth.isChallengeMandated());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertTrue(secureEcom.isChallengeMandated());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(initAuth.getIssuerAcsUrl());
        String authResponse = acsClient.authenticate_v2(initAuth);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void DecoupledAuth() throws ApiException {
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());
        tokenizedCard.setCardHolderName("James Mason");

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withDecoupledNotificationUrl("https://www.example.com/decoupledNotification")
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withDecoupledFlowRequest(true)
                        .withDecoupledFlowTimeout(9001)
                        .withDecoupledNotificationUrl("https://www.example.com/decoupledNotification")
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void ExemptionSaleTransaction() throws ApiException {
        card.setNumber(CARD_CHALLENGE_REQUIRED_V2_2.cardNumber);

        ThreeDSecure threeDS = new ThreeDSecure();
        threeDS.setExemptStatus(ExemptStatus.LowValue);

        card.setThreeDSecure(threeDS);

        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FrictionlessFullCycle_v2_Verify3DS() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
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
                        .withCustomerEmail("jason@globalpay.com")
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());

        card.setThreeDSecure(secureEcom);

        Transaction verifyResponse =
                card
                        .verify()
                        .withCurrency(currency)
                        .execute();
        assertNotNull(verifyResponse);
        assertEquals(SUCCESS, verifyResponse.getResponseCode());
        assertEquals("VERIFIED", verifyResponse.getResponseMessage());

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FrictionlessFullCycle_v2_Verify3DS_TokenizedCard() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertTrue(secureEcom.isEnrolled());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute();

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());

        tokenizedCard.setThreeDSecure(secureEcom);

        Transaction verifyResponse =
                tokenizedCard
                        .verify()
                        .withCurrency(currency)
                        .execute();
        assertNotNull(verifyResponse);
        assertEquals(SUCCESS, verifyResponse.getResponseCode());
        assertEquals("VERIFIED", verifyResponse.getResponseMessage());

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    /**
     * ACS Authentication Simulator result codes
     */
    public enum AuthenticationResultCode implements INumericConstant {
        Successful(0),
        Unavailable(5),
        AttemptAcknowledge(7),
        Failed(9);

        int value;

        AuthenticationResultCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    /**
     * This 3DS ACS client mocks the ACS Authentication Simulator used for testing purposes
     */
    public static class GpApi3DSecureAcsClient {

        public GpApi3DSecureAcsClient(String redirectUrl) {
        }

        private String submitFormData(String formUrl, List<HashMap<String, String>> formData) throws GatewayException {
            HttpsURLConnection httpClient;
            try {
                httpClient = (HttpsURLConnection) new URL((formUrl).trim()).openConnection();

                httpClient.setRequestMethod("POST");
                httpClient.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpClient.setConnectTimeout(60000);
                httpClient.setSSLSocketFactory(new SSLSocketFactoryEx());
                httpClient.setDoInput(true);
                httpClient.setDoOutput(true);

                DataOutputStream out = new DataOutputStream(httpClient.getOutputStream());

                HashMap<String, String> data = formData.get(0);

                Iterator keyIter = data.keySet().iterator();
                String content = "";
                for (int i = 0; keyIter.hasNext(); i++) {
                    Object key = keyIter.next();
                    if (i != 0) {
                        content += "&";
                    }
                    content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
                }

                out.writeBytes(content);
                out.flush();
                out.close();

                return IOUtils.readFully(httpClient.getInputStream());

            } catch (Exception e) {
                throw new GatewayException("Error occurred while communicating with gateway.", e);
            }

        }

        private String getFormAction(String rawHtml, String formName) {
            String searchString = "name=\"" + formName + "\" action=\"";

            int index = rawHtml.indexOf(searchString);
            if (index > -1) {
                index = index + searchString.length();
                int length = rawHtml.indexOf("\"", index) - index;
                return rawHtml.substring(index, index + length);
            }
            return null;
        }

        private String getInputValue(String rawHtml, String inputName) {
            String searchString = "name=\"" + inputName + "\" value=\"";

            int index = rawHtml.indexOf(searchString);
            if (index > -1) {
                index = index + searchString.length();
                int length = rawHtml.indexOf("\"", index) - index;
                return rawHtml.substring(index, index + length);
            }
            return null;
        }

        /**
         * Performs ACS authentication for 3DS v2
         *
         * @return Raw html result
         **/
        public String authenticate_v2(ThreeDSecure secureEcom) throws GatewayException, InterruptedException {
            // Step 1
            ArrayList<HashMap<String, String>> formData = new ArrayList<>();
            HashMap<String, String> keyValues = new HashMap<>();

            keyValues.put(secureEcom.getMessageType(), secureEcom.getPayerAuthenticationRequest());
            keyValues.put(secureEcom.getSessionDataFieldName(), secureEcom.getServerTransactionId());

            formData.add(keyValues);

            String rawResponse = submitFormData(secureEcom.getIssuerAcsUrl(), formData);

            // Step 2
            formData = new ArrayList<>();
            keyValues = new HashMap<>();

            keyValues.put("get-status-type", "true");

            formData.add(keyValues);

            do {
                rawResponse = submitFormData(secureEcom.getIssuerAcsUrl(), formData);
                Thread.sleep(5000);
            } while (rawResponse.trim().equals("IN_PROGRESS"));

            // Step 3
            formData = new ArrayList<>();
            keyValues = new HashMap<>();

            formData.add(keyValues);

            rawResponse = submitFormData(secureEcom.getIssuerAcsUrl(), formData);

            // Step 4
            formData = new ArrayList<>();
            keyValues = new HashMap<>();

            keyValues.put("cres", getInputValue(rawResponse, "cres"));

            formData.add(keyValues);

            rawResponse = submitFormData(getFormAction(rawResponse, "ResForm"), formData);

            return rawResponse;
        }

    }
}