package com.global.api.tests.secure3d;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class Secure3dServiceTests {
    private CreditCardData card = new CreditCardData();
    private RecurringPaymentMethod stored;
    private Address shippingAddress;
    private Address billingAddress;
    private BrowserData browserData;

    public Secure3dServiceTests() throws ApiException {
        GpApiConfig config = new GpApiConfig();
        config.setAppId("P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg");
        config.setAppKey("ockJr6pv6KFoGiZA");
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");

        ServicesContainer.configureService(config);

        // create card data
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // stored card
        stored = new RecurringPaymentMethod(
                "20190809-Realex",
                "20190809-Realex-Credit"
        );

        // shipping address
        shippingAddress = new Address();
        shippingAddress.setStreetAddress1("Apartment 852");
        shippingAddress.setStreetAddress2("Complex 741");
        shippingAddress.setStreetAddress3("no");
        shippingAddress.setCity("Chicago");
        shippingAddress.setPostalCode("5001");
        shippingAddress.setState("IL");
        shippingAddress.setCountryCode("840");

        // billing address
        billingAddress = new Address();
        billingAddress.setStreetAddress1("Flat 456");
        billingAddress.setStreetAddress2("House 789");
        billingAddress.setStreetAddress3("no");
        billingAddress.setCity("Halifax");
        billingAddress.setPostalCode("W5 9HR");
        billingAddress.setCountryCode("826");

        // browser data
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
    public void fullCycle_v1() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .withAmount(new BigDecimal(1))
                .withCurrency("USD")
                .execute(Secure3dVersion.ONE);

        assertNotNull(secureEcom);
        assertTrue("Card not enrolled", secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .execute("GpApiConfig");

        assertNotNull(secureEcom);

        card.setThreeDSecure(secureEcom);

        assertEquals("SUCCESS_AUTHENTICATED", secureEcom.getStatus());

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured, response.getResponseMessage());

    }

    @Test
    public void cardHolderEnrolled_ChallengeRequired_v1() throws ApiException {
        card = new CreditCardData();

        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE);

        assertNotNull(secureEcom);
        assertTrue("Card not enrolled", secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals("CHALLENGE_REQUIRED", secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
    }

    @Test
    public void cardHolderNotEnrolled_v1() throws ApiException {
        card = new CreditCardData();
        card.setNumber("4917000000000087");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertFalse(secureEcom.isEnrolled());
        assertEquals("NOT_ENROLLED", secureEcom.getStatus());
    }

    @Test
    public void cardHolderNotEnrolled_v1_WithTokenizedPaymentMethod() throws ApiException {
        card = new CreditCardData();
        card.setNumber("4917000000000087");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.ONE);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertFalse(secureEcom.isEnrolled());
        assertEquals("NOT_ENROLLED", secureEcom.getStatus());
    }

    @Test
    public void fullCycle_v2() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);
        assertFalse(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(initAuth);
        assertEquals("SUCCESS_AUTHENTICATED", initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO);

        assertEquals("SUCCESS_AUTHENTICATED", secureEcom.getStatus());

        card.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                card
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured, response.getResponseMessage());
    }

    @Test
    public void fullCycle_v2_WithTokenizedPaymentMethod() throws ApiException {
        // Frictionless scenario
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Doe");

        // Tokenize payment method
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize());

        assertNotNull(tokenizedCard.getToken());

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);
        assertTrue("Card not enrolled", secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(tokenizedCard, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(initAuth);
        assertEquals("SUCCESS_AUTHENTICATED", initAuth.getStatus());

        // Get authentication data
        secureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute(Secure3dVersion.TWO);

        assertEquals("SUCCESS_AUTHENTICATED", secureEcom.getStatus());

        tokenizedCard.setThreeDSecure(secureEcom);

        // Create transaction
        Transaction response =
                tokenizedCard
                        .charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute("GpApiConfig");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured, response.getResponseMessage());
    }

    @Test
    public void cardHolderEnrolled_ChallengeRequired_v2() throws ApiException {
        // Challenge required scenario
        card = new CreditCardData();
        card.setNumber("4222000001227408");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");

        // Check enrollment
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency("USD")
                        .withAmount(new BigDecimal("10.01"))
                        .execute(Secure3dVersion.TWO);

        assertNotNull(secureEcom);
        assertTrue( "Card not enrolled", secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());
        assertEquals("AVAILABLE", secureEcom.getStatus());

        // Initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .withAuthenticationSource(AuthenticationSource.Browser)
                        .withMethodUrlCompletion(MethodUrlCompletion.Yes)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .execute(Secure3dVersion.TWO);

        assertNotNull(initAuth);
        assertEquals("CHALLENGE_REQUIRED", initAuth.getStatus());
        assertTrue(initAuth.isChallengeMandated());
        assertNotNull(initAuth.getIssuerAcsUrl());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
    }

}
