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
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.*;
import static org.junit.Assert.*;

public class GpApi3DSecureTests extends BaseGpApiTest {

    private final static String AVAILABLE = "AVAILABLE";
    private final static String FAILED = "FAILED";
    private final static String NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
    private final static String SUCCESS_ATTEMPT_MADE = "SUCCESS_ATTEMPT_MADE";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String ENROLLED = "ENROLLED";
    private final static String NOT_ENROLLED = "NOT_ENROLLED";
    private final static String SUCCESS_AUTHENTICATED = "SUCCESS_AUTHENTICATED";

    private CreditCardData card;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BrowserData browserData;
    private final StoredCredential storedCredential;
    private final MobileData mobileData;

    private static final BigDecimal amount = new BigDecimal("10.01");
    private static final String currency = "GBP";

    public GpApi3DSecureTests() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config.setAppId(APP_ID);
        config.setAppKey(APP_KEY);
        config.setCountry("GB");
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

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
                                JsonDoc.parse(  "{" +
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
    public void FullCycle_v1() throws ApiException {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withChallengeRequestIndicator(ChallengeRequestIndicator.ChallengeMandated)
                        .withStoredCredential(storedCredential)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertNotNull(secureEcom.getSessionDataFieldName());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, null);

        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v1_WithTokenizedPaymentMethod() throws Exception {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getMessageType());
        assertNotNull(secureEcom.getSessionDataFieldName());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, null);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationUnavailable_v1() throws Exception {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getMessageType());
        assertNotNull(secureEcom.getSessionDataFieldName());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, AuthenticationResultCode.Unavailable);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(FAILED, secureEcom.getStatus());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationAttemptAcknowledge_v1() throws Exception {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getMessageType());
        assertNotNull(secureEcom.getSessionDataFieldName());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, AuthenticationResultCode.AttemptAcknowledge);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_ATTEMPT_MADE, secureEcom.getStatus());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationFailed_v1() throws Exception {
        AuthenticationResultCode[] authenticationResultCode = {AuthenticationResultCode.Unavailable, AuthenticationResultCode.Failed, AuthenticationResultCode.AttemptAcknowledge};
        String[] status = {FAILED, NOT_AUTHENTICATED, SUCCESS_ATTEMPT_MADE};

        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        for (int i = 0; i < status.length; i++) {
            // Check enrollment
            ThreeDSecure secureEcom =
                    Secure3dService
                            .checkEnrollment(card)
                            .withCurrency(currency)
                            .withAmount(amount)
                            .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

            assertNotNull(secureEcom);
            assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
            assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
            assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
            assertTrue(secureEcom.isChallengeMandated());
            assertNotNull(secureEcom.getIssuerAcsUrl());
            assertNotNull(secureEcom.getPayerAuthenticationRequest());
            assertNotNull(secureEcom.getChallengeReturnUrl());
            assertNotNull(secureEcom.getMessageType());
            assertNotNull(secureEcom.getSessionDataFieldName());

            // Perform ACS authentication
            GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
            StringBuffer payerAuthenticationResponse = new StringBuffer("");
            String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, authenticationResultCode[i]);
            assertEquals("{\"success\":true}", authResponse);

            // Get authentication data
            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                            .withServerTransactionId(secureEcom.getServerTransactionId())
                            .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

            assertNotNull(secureEcom);
            assertEquals(status[i], secureEcom.getStatus());
            String liabilityShift = (status[i] == "SUCCESS_ATTEMPT_MADE") ? "YES" : "NO";
            assertEquals(liabilityShift, secureEcom.getLiabilityShift());
        }
    }

    @Test
    public void CardHolderNotEnrolled_v1() throws ApiException {
        card.setNumber(CARDHOLDER_NOT_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(NOT_ENROLLED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());
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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

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
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_CreditSale_WithStoredCredentials_RecurringPayment() throws ApiException {
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertNull(secureEcom.getEci());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response2);
        assertEquals(SUCCESS, response2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response2.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_WithMobileSdk() throws ApiException {
        // Frictionless scenario
        card.setNumber(CARD_AUTH_SUCCESSFUL_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(CHALLENGE_REQUIRED, initAuth.getStatus());
        assertTrue(initAuth.isChallengeMandated());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(initAuth.getIssuerAcsUrl());
        String authResponse = acsClient.authenticate_v2(initAuth);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_v2_WithMobileSdk() throws Exception {
        // Challenge required scenario
        card.setNumber(CARD_CHALLENGE_REQUIRED_V2_1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(CHALLENGE_REQUIRED, initAuth.getStatus());
        assertTrue(initAuth.isChallengeMandated());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertNotNull(initAuth.getAcsInterface());
        assertNotNull(initAuth.getAcsUiTemplate());

        // Perform ACS authentication
        GpApi3DSecureAcsClient acsClient = new GpApi3DSecureAcsClient(initAuth.getIssuerAcsUrl());
        String authResponse = acsClient.authenticate_v2(initAuth);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals("YES", secureEcom.getLiabilityShift());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute(GP_API_CONFIG_NAME);

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertEquals(amount, secureEcom.getAmount());

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

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
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

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
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationFailed_v1_WrongAcsValue() throws Exception {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .withServerTransactionId(secureEcom.getServerTransactionId())
                    .withPayerAuthenticationResponse(UUID.randomUUID().toString())
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50020", ex.getResponseText());
            assertEquals("Status Code: 400 - Unable to decompress the PARes.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
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
         * Performs ACS authentication for 3DS v1
         *
         * @return Raw html result
         **/
        public String authenticate_v1(ThreeDSecure secureEcom, StringBuffer paRes, AuthenticationResultCode authenticationResultCode) throws GatewayException {

            if (authenticationResultCode == null) {
                authenticationResultCode = AuthenticationResultCode.Successful;
            }

            // Step 1
            ArrayList<HashMap<String, String>> formData = new ArrayList<>();
            HashMap<String, String> keyValues = new HashMap<>();

            keyValues.put(secureEcom.getMessageType(), secureEcom.getPayerAuthenticationRequest());
            keyValues.put(secureEcom.getSessionDataFieldName(), secureEcom.getServerTransactionId());
            keyValues.put("TermUrl", secureEcom.getChallengeReturnUrl());
            keyValues.put("AuthenticationResultCode", String.valueOf(authenticationResultCode.getValue()));

            formData.add(keyValues);

            String rawResponse = submitFormData(secureEcom.getIssuerAcsUrl(), formData);

            paRes.append(getInputValue(rawResponse, "PaRes"));

            // Step 2
            formData = new ArrayList<>();
            keyValues = new HashMap<>();

            keyValues.put("MD", getInputValue(rawResponse, "MD"));
            keyValues.put("PaRes", paRes.toString());

            formData.add(keyValues);

            rawResponse = submitFormData(getFormAction(rawResponse, "PAResForm"), formData);

            return rawResponse;
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