package com.global.api.tests.gpEcom;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GpEcomConfig;
import com.global.api.services.Secure3dService;
import com.global.api.tests.AcsResponse;
import com.global.api.tests.ThreeDSecureAcsClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.*;

public class GpEcomSecure3dServiceTest extends BaseGpEComTest {
    private CreditCardData card;
    private RecurringPaymentMethod stored;
    private Address shippingAddress;
    private Address billingAddress;
    private BrowserData browserData;

    private final BigDecimal amount = new BigDecimal("10.02");
    private final String currency = "USD";

    public GpEcomSecure3dServiceTest() throws ApiException {
        GpEcomConfig config = gpEComSetup();
        config.setMerchantId("myMerchantId");
        config.setAccountId("ecom3ds");
        ServicesContainer.configureService(config);

        // create card data
        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(DateTime.now().getMonthOfYear());
        card.setExpYear(DateTime.now().getYear() + 1);
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
    public void fullCycle_v2() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoChallengeRequested)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void fullCycle_v2_FrictionlessCards() throws ApiException {
        HashMap<String, String> enrolledCards = new HashMap<>();
        enrolledCards.put("4263970000005262", "2.1.0");
        enrolledCards.put("4222000006724235", "2.1.0");
        enrolledCards.put("4222000006285344", "2.2.0");
        enrolledCards.put("4222000009719489", "2.2.0");

        for (String cardNumber : enrolledCards.keySet()) {
            card.setNumber(cardNumber);
            // check enrollment
            ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                    .execute(Secure3dVersion.TWO);
            assertNotNull(secureEcom);

            if (secureEcom.isEnrolled()) {
                assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

                // initiate authentication
                ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withMethodUrlCompletion(MethodUrlCompletion.No)
                        .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                        .execute();
                assertNotNull(initAuth);
                assertEquals("AUTHENTICATION_SUCCESSFUL", initAuth.getStatus());
                assertNotNull(initAuth.getAcsTransactionId());
                assertNotNull(initAuth.getAcsReferenceNumber());
                assertNotNull(initAuth.getAuthenticationValue());
                assertNotNull(initAuth.getServerTransactionId());
                assertEquals("05", initAuth.getEci());
                assertEquals(enrolledCards.get(cardNumber), initAuth.getAcsEndVersion());

                // get authentication data
                secureEcom = Secure3dService.getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();
                card.setThreeDSecure(secureEcom);

                if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                    Transaction response = card.charge(amount)
                            .withCurrency(currency)
                            .execute();
                    assertNotNull(response);
                    assertEquals("00", response.getResponseCode());
                } else fail("Signature verification failed.");
            } else fail("Card not enrolled.");
        }
    }

    @Test
    public void fullCycle_v2_ChallengeRequired() throws ApiException {
        HashMap<String, String> enrolledCards = new HashMap<>();
        enrolledCards.put("4012001038488884", "2.1.0");
        enrolledCards.put("4222000001227408", "2.2.0");

        for (String cardNumber : enrolledCards.keySet()) {
            card.setNumber(cardNumber);

            // check enrollment
            ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                    .execute(Secure3dVersion.TWO);
            assertNotNull(secureEcom);

            assertTrue(secureEcom.isEnrolled());
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService
                    .initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .execute();

            assertNotNull(initAuth);
            assertEquals("CHALLENGE_REQUIRED", initAuth.getStatus());
            assertNotNull(initAuth.getPayerAuthenticationRequest());
            assertNotNull(initAuth.getAcsTransactionId());
            assertNotNull(initAuth.getAcsReferenceNumber());
            assertNotNull(initAuth.getAuthenticationType());
            assertNotNull(initAuth.getPayerAuthenticationRequest());
            assertNull(initAuth.getEci());
            assertEquals(enrolledCards.get(cardNumber), initAuth.getMessageVersion());

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            assertEquals("CHALLENGE_REQUIRED", secureEcom.getStatus());

            Transaction response = card.charge(amount)
                    .withCurrency(currency)
                    .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        }
    }

    @Test
    public void fullCycle_Any() throws ApiException {
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .withAmount(amount)
                .withCurrency(currency)
                .execute(Secure3dVersion.ANY);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            if (secureEcom.getVersion().equals(Secure3dVersion.TWO)) {
                assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

                // initiate authentication
                ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withMethodUrlCompletion(MethodUrlCompletion.No)
                        .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                        .execute();
                assertNotNull(initAuth);

                // get authentication data
                secureEcom = Secure3dService.getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();
                card.setThreeDSecure(secureEcom);

                if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                    Transaction response = card.charge(amount)
                            .withCurrency(currency)
                            .execute();
                    assertNotNull(response);
                    assertEquals("00", response.getResponseCode());
                } else fail("Signature verification failed.");
            } else {
                // authenticate
                ThreeDSecureAcsClient authClient = new ThreeDSecureAcsClient(secureEcom.getIssuerAcsUrl());
                AcsResponse authResponse = authClient.authenticate(secureEcom.getPayerAuthenticationRequest(), secureEcom.getMerchantData().toString());

                String payerAuthenticationResponse = authResponse.getAuthResponse();
                MerchantDataCollection md = MerchantDataCollection.parse(authResponse.getMerchantData());

                // verify signature through the service and affix to the card object
                secureEcom = Secure3dService.getAuthenticationData()
                        .withPayerAuthenticationResponse(payerAuthenticationResponse)
                        .withMerchantData(md)
                        .execute();
                card.setThreeDSecure(secureEcom);

                if (secureEcom.getStatus().equals("Y")) {
                    Transaction response = card.charge()
                            .execute();
                    assertNotNull(response);
                    assertEquals("00", response.getResponseCode());
                } else fail("Signature verification failed.");
            }
        } else fail("Card not enrolled.");
    }

    @Test
    public void fullCycle_v2_StoredCard() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(stored)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(stored, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = stored.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void fullCycle_v2_OTB() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.verify()
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void fullCycle_v2_OTB_StoredCard() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(stored)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(stored, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = stored.verify()
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalRequestLevelFields() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withMerchantInitiatedRequestType(MerchantInitiatedRequestType.RecurringTransaction)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalOrderLevelFields() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    //.withGiftCardCount(1)
                    .withGiftCardCurrency("USD")
                    .withGiftCardAmount(new BigDecimal("250.00"))
                    .withDeliveryEmail("james.mason@example.com")
                    .withDeliveryTimeFrame(DeliveryTimeFrame.ElectronicDelivery)
                    .withShippingMethod(ShippingMethod.VerifiedAddress)
                    .withShippingNameMatchesCardHolderName(true)
                    .withPreOrderIndicator(PreOrderIndicator.FutureAvailability)
                    .withPreOrderAvailabilityDate(DateTime.parse("2019-04-18"))
                    .withReorderIndicator(ReorderIndicator.Reorder)
                    .withOrderTransactionType(OrderTransactionType.GoodsAndServicesPurchase)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalPayerLevelFields() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withCustomerAccountId("6dcb24f5-74a0-4da3-98da-4f0aa0e88db3")
                    .withAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                    .withAccountCreateDate(DateTime.parse("2019-01-10"))
                    .withAccountChangeDate(DateTime.parse("2019-01-28"))
                    .withAccountChangeIndicator(AgeIndicator.ThisTransaction)
                    .withPasswordChangeDate(DateTime.parse("2019-01-15"))
                    .withPasswordChangeIndicator(AgeIndicator.LessThanThirtyDays)
                    .withHomeNumber("44", "123456798")
                    .withWorkNumber("44", "1801555888")
                    .withPaymentAccountCreateDate(DateTime.parse("2019-01-01"))
                    .withPaymentAccountAgeIndicator(AgeIndicator.LessThanThirtyDays)
                    .withPreviousSuspiciousActivity(false)
                    .withNumberOfPurchasesInLastSixMonths(3)
                    .withNumberOfTransactionsInLast24Hours(1)
                    .withNumberOfTransactionsInLastYear(5)
                    .withNumberOfAddCardAttemptsInLast24Hours(1)
                    .withShippingAddressCreateDate(DateTime.parse("2019-01-28"))
                    .withShippingAddressUsageIndicator(AgeIndicator.ThisTransaction)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalPriorAuthenticationData() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withPriorAuthenticationMethod(PriorAuthenticationMethod.FrictionlessAuthentication)
                    .withPriorAuthenticationTransactionId("26c3f619-39a4-4040-bf1f-6fd433e6d615")
                    .withPriorAuthenticationTimestamp(DateTime.parse("2019-01-10T12:57:33.333Z", DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
                    .withPriorAuthenticationData("string")
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalRecurringData() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withMaxNumberOfInstallments(5)
                    .withRecurringAuthorizationFrequency(25)
                    .withRecurringAuthorizationExpiryDate(DateTime.parse("2019-08-25"))
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalPayerLoginData() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .withChallengeRequestIndicator(ChallengeRequestIndicator.NoPreference)

                    // optionals
                    .withCustomerAuthenticationData("string")
                    .withCustomerAuthenticationTimestamp(DateTime.parse("2019-01-28T12:57:33.333Z", DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")))
                    .withCustomerAuthenticationMethod(CustomerAuthenticationMethod.MerchantSystem)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if (secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(amount)
                        .withCurrency(currency)
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            } else fail("Signature verification failed.");
        } else fail("Card not enrolled.");
    }

    @Test
    public void optionalMobileFields() throws ApiException {
        //card number for optional mobile fields
        card.setNumber("4012001038488884");

        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

        String ephemeralPublicKey = "{" +
                "\"kty\":\"EC\"," +
                "\"crv\":\"P-256\"," +
                "\"x\":\"WWcpTjbOqiu_1aODllw5rYTq5oLXE_T0huCPjMIRbkI\"," +
                "\"y\":\"Wz_7anIeadV8SJZUfr4drwjzuWoUbOsHp5GdRZBAAiw\"" +
                "}";

        // initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .withOrderCreateDate(DateTime.now())
                        .withOrderId(secureEcom.getOrderId())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddressMatchIndicator(false)
                        .withMethodUrlCompletion(MethodUrlCompletion.No)
                        .withMessageCategory(MessageCategory.PaymentAuthentication)
                        .withCustomerEmail("customer@domain.com")
                        // optionals
                        .withApplicationId("f283b3ec-27da-42a1-acea-f3f70e75bbdc")
                        .withSdkInterface(SdkInterface.Both)
                        .withSdkUiTypes(SdkUiType.Text, SdkUiType.SingleSelect, SdkUiType.MultiSelect, SdkUiType.OOB, SdkUiType.HTML_Other)
                        .withReferenceNumber("3DS_LOA_SDK_PPFU_020100_00007")
                        .withSdkTransactionId("b2385523-a66c-4907-ac3c-91848e8c0067")
                        .withEncodedData("ew0KCSJEViI6ICIxLjAiLA0KCSJERCI6IHsNCgkJIkMwMDEiOiAiQW5kcm9pZCIsDQoJCSJDMDAyIjogIkhUQyBPbmVfTTgiLA0KCQkiQzAwNCI6ICI1LjAuMSIsDQoJCSJDMDA1IjogImVuX1VTIiwNCgkJIkMwMDYiOiAiRWFzdGVybiBTdGFuZGFyZCBUaW1lIiwNCgkJIkMwMDciOiAiMDY3OTc5MDMtZmI2MS00MWVkLTk0YzItNGQyYjc0ZTI3ZDE4IiwNCgkJIkMwMDkiOiAiSm9obidzIEFuZHJvaWQgRGV2aWNlIg0KCX0sDQoJIkRQTkEiOiB7DQoJCSJDMDEwIjogIlJFMDEiLA0KCQkiQzAxMSI6ICJSRTAzIg0KCX0sDQoJIlNXIjogWyJTVzAxIiwgIlNXMDQiXQ0KfQ0K")
                        .withMaximumTimeout(5)
                        .withEphemeralPublicKey(ephemeralPublicKey)
                        .execute();

        assertNotNull(initAuth);
        assertEquals("CHALLENGE_REQUIRED", initAuth.getStatus());
        assertNotNull(initAuth.getPayerAuthenticationRequest());
        assertNotNull(initAuth.getAcsTransactionId());
        assertNotNull(initAuth.getAcsUiTemplate());
        assertNotNull(initAuth.getAcsInterface());
        assertNotNull(initAuth.getAcsReferenceNumber());

        // get authentication data
        secureEcom = Secure3dService.getAuthenticationData()
                .withServerTransactionId(initAuth.getServerTransactionId())
                .execute();
        card.setThreeDSecure(secureEcom);

        assertEquals("CHALLENGE_REQUIRED", secureEcom.getStatus());

        Transaction response = card.charge(amount)
                .withCurrency(currency)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void optionalMobileFields_frictionlessCard_noNullpointerException() throws ApiException {
        //card number for optional mobile fields
        card.setNumber("4263970000005262");

        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

        String ephemeralPublicKey = "{" +
                "\"kty\":\"EC\"," +
                "\"crv\":\"P-256\"," +
                "\"x\":\"WWcpTjbOqiu_1aODllw5rYTq5oLXE_T0huCPjMIRbkI\"," +
                "\"y\":\"Wz_7anIeadV8SJZUfr4drwjzuWoUbOsHp5GdRZBAAiw\"" +
                "}";

        // initiate authentication
        ThreeDSecure initAuth =
                Secure3dService
                        .initiateAuthentication(card, secureEcom)
                        .withAmount(amount)
                        .withCurrency(currency)
                        .withAuthenticationSource(AuthenticationSource.MobileSDK)
                        .withOrderCreateDate(DateTime.now())
                        .withOrderId(secureEcom.getOrderId())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withAddressMatchIndicator(false)
                        .withMethodUrlCompletion(MethodUrlCompletion.No)
                        .withMessageCategory(MessageCategory.PaymentAuthentication)
                        .withCustomerEmail("customer@domain.com")
                        // optionals
                        .withApplicationId("f283b3ec-27da-42a1-acea-f3f70e75bbdc")
                        .withSdkInterface(SdkInterface.Both)
                        .withSdkUiTypes(SdkUiType.Text, SdkUiType.SingleSelect, SdkUiType.MultiSelect, SdkUiType.OOB, SdkUiType.HTML_Other)
                        .withReferenceNumber("3DS_LOA_SDK_PPFU_020100_00007")
                        .withSdkTransactionId("b2385523-a66c-4907-ac3c-91848e8c0067")
                        .withEncodedData("ew0KCSJEViI6ICIxLjAiLA0KCSJERCI6IHsNCgkJIkMwMDEiOiAiQW5kcm9pZCIsDQoJCSJDMDAyIjogIkhUQyBPbmVfTTgiLA0KCQkiQzAwNCI6ICI1LjAuMSIsDQoJCSJDMDA1IjogImVuX1VTIiwNCgkJIkMwMDYiOiAiRWFzdGVybiBTdGFuZGFyZCBUaW1lIiwNCgkJIkMwMDciOiAiMDY3OTc5MDMtZmI2MS00MWVkLTk0YzItNGQyYjc0ZTI3ZDE4IiwNCgkJIkMwMDkiOiAiSm9obidzIEFuZHJvaWQgRGV2aWNlIg0KCX0sDQoJIkRQTkEiOiB7DQoJCSJDMDEwIjogIlJFMDEiLA0KCQkiQzAxMSI6ICJSRTAzIg0KCX0sDQoJIlNXIjogWyJTVzAxIiwgIlNXMDQiXQ0KfQ0K")
                        .withMaximumTimeout(5)
                        .withEphemeralPublicKey(ephemeralPublicKey)
                        .execute();

        // get authentication data
        secureEcom = Secure3dService.getAuthenticationData()
                .withServerTransactionId(initAuth.getServerTransactionId())
                .execute();
        card.setThreeDSecure(secureEcom);
    }

    @Test
    public void checkVersion_Not_Enrolled() throws ApiException {
        card.setNumber("4917000000000087");

        boolean errorFound = false;
        try {
            Secure3dService
                    .checkEnrollment(card)
                    .withAmount(amount)
                    .withCurrency(currency)
                    .execute(Secure3dVersion.ONE);
        } catch (ConfigurationException e) {
            errorFound = true;
            assertEquals("Secure 3d is not configured for ONE", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test(expected = BuilderException.class)
    public void checkVersion_Not_ISecure3d() throws ApiException {
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(new DebitTrackData())
                .execute(Secure3dVersion.ANY);
        assertNotNull(secureEcom);
    }

    @Test
    public void recurringTest() throws ApiException {
        // customer initiated - add SCA (3D Secure 2) data
        ThreeDSecure threeDSecureData = new ThreeDSecure();
        threeDSecureData.setAuthenticationValue("ODQzNjgwNjU0ZjM3N2JmYTg0NTM=");
        threeDSecureData.setDirectoryServerTransactionId("c272b04f-6e7b-43a2-bb78-90f4fb94aa25");
        threeDSecureData.setEci("05");
        threeDSecureData.setMessageVersion("2.1.0");

        // add the 3D Secure 2 data to the card object
        RecurringPaymentMethod paymentMethod = new RecurringPaymentMethod(
                "20190809-Realex",
                "20190809-Realex-Credit"
        );
        paymentMethod.setThreeDSecure(threeDSecureData);

        Transaction response = paymentMethod.charge(amount)
                .withCurrency(currency)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
