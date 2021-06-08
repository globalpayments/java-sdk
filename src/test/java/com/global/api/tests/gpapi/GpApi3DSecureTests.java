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
import org.joda.time.DateTime;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.global.api.tests.gpapi.GpApi3DSTestCards.CARDHOLDER_ENROLLED_V1;
import static com.global.api.tests.gpapi.GpApi3DSTestCards.CARD_AUTH_SUCCESSFUL_V2_2;
import static org.junit.Assert.*;

public class GpApi3DSecureTests extends BaseGpApiTest {

    private final static String AVAILABLE = "AVAILABLE";
    private final static String AUTHENTICATION_COULD_NOT_BE_PERFORMED = "AUTHENTICATION_COULD_NOT_BE_PERFORMED";
    private final static String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    private final static String AUTHENTICATION_SUCCESSFUL = "AUTHENTICATION_SUCCESSFUL";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String NOT_ENROLLED = "NOT_ENROLLED";
    private final static String SUCCESS_AUTHENTICATED = "SUCCESS_AUTHENTICATED";

    private CreditCardData card;
    private RecurringPaymentMethod stored;
    private final Address shippingAddress;
    private final Address billingAddress;
    private final BrowserData browserData;

    public GpApi3DSecureTests() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();
        config.setAppId("P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg");
        config.setAppKey("ockJr6pv6KFoGiZA");
        config.setCountry("GB");
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        // Create card data
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Stored card
        stored = new RecurringPaymentMethod("20190809-Realex", "20190809-Realex-Credit");

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
    }

    @Test
    public void FullCycle_v1() throws ApiException {

        card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Unscheduled);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.NoShow);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withChallengeRequestIndicator(ChallengeRequestIndicator.ChallengeMandated)
                        // TODO: Enable this when possible
                        //.withStoredCredential(storedCredential)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        // assertTrue(secureEcom.isEnrolled());
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
        assertEquals(AUTHENTICATION_SUCCESSFUL, secureEcom.getStatus());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void fullCycle_v1_WithTokenizedPaymentMethod() throws Exception {

        card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        //assertTrue(secureEcom.isEnrolled());
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
        assertEquals(AUTHENTICATION_SUCCESSFUL, secureEcom.getStatus());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void cardHolderEnrolled_ChallengeRequired_AuthenticationUnavailable_v1() throws Exception {

        card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");


        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertFalse(secureEcom.isEnrolled());
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
        assertEquals(AUTHENTICATION_COULD_NOT_BE_PERFORMED, secureEcom.getStatus());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationAttemptAcknowledge_v1() throws Exception {

        card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        //assertTrue(secureEcom.isEnrolled());
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
        // TODO: Status should have a value
        assertEquals("", secureEcom.getStatus());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_AuthenticationFailed_v1() throws Exception {

        card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        //assertTrue(secureEcom.isEnrolled());
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
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, AuthenticationResultCode.Failed);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(AUTHENTICATION_FAILED, secureEcom.getStatus());
    }

    @Test
    public void CardHolderNotEnrolled_v1() throws ApiException {
        card = new CreditCardData();
        card.setNumber("4917000000000087");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertFalse(secureEcom.isEnrolled());  // TODO: See if need to be change to an Enum value
        assertEquals(NOT_ENROLLED, secureEcom.getStatus());
    }

    @Test
    public void FullCycle_v2() throws ApiException {

        // Frictionless scenario
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Doe");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        // assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);
        storedCredential.setType(StoredCredentialType.Unscheduled);
        storedCredential.setSequence(StoredCredentialSequence.First);
        storedCredential.setReason(StoredCredentialReason.NoShow);

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        // TODO: Enable this when possible
                        //.withStoredCredential(storedCredential)
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void FullCycle_v2_WithTokenizedPaymentMethod() throws ApiException {

        // Frictionless scenario
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Doe");

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        // assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(initAuth);
        assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void CardHolderEnrolled_ChallengeRequired_v2() throws Exception {
        // Challenge required scenario
        card = new CreditCardData();
        card.setNumber("4012001038488884");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        //assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals(AVAILABLE, secureEcom.getStatus());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("GBP")
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

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
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
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(AVAILABLE, secureEcom.getStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertNull(secureEcom.getEci());
        assertEquals(new BigDecimal("1001"), secureEcom.getAmount());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("15"))
                        .withCurrency("EUR")
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
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertNotNull(initAuth.getChallengeReturnUrl());
        assertNotNull(initAuth.getSessionDataFieldName());
        assertNotNull(initAuth.getMessageType());
        assertNull(initAuth.getEci());
        assertEquals(new BigDecimal("1001"), initAuth.getAmount());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNull(secureEcom.getEci());
        assertEquals(new BigDecimal("1001"), secureEcom.getAmount());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("GBP")
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }
	
	@Test
    public void testCardHolderEnrolled_ChallengeRequired_AuthenticationFailed_v1_WrongAcsValue() throws Exception {
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("GBP")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertFalse(secureEcom.isEnrolled());
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
        private final String _redirectUrl;

        public GpApi3DSecureAcsClient(String redirectUrl) {
            _redirectUrl = redirectUrl;
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

            //TODO: use secureEcom.getMessageType() instead of "creq"
            keyValues.put("creq", secureEcom.getPayerAuthenticationRequest());
            //TODO: use secureEcom.getSessionDataFieldName() instead of "threeDSSessionData"
            keyValues.put("threeDSSessionData", secureEcom.getServerTransactionId());

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