package com.global.api.tests.secure3d;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.MerchantDataCollection;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.Secure3dService;
import com.global.api.tests.AcsResponse;
import com.global.api.tests.ThreeDSecureAcsClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class Secure3dServiceTests {
    private CreditCardData card;
    private RecurringPaymentMethod stored;
    private Address shippingAddress;
    private Address billingAddress;
    private BrowserData browserData;

    public Secure3dServiceTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setMerchantId("myMerchantId");
        config.setAccountId("ecom3ds");
        config.setSharedSecret("secret");
        config.setEnableLogging(true);
        config.setMethodNotificationUrl("https://www.example.com/methodNotificationUrl");
        config.setChallengeNotificationUrl("https://www.example.com/challengeNotificationUrl");
        config.setSecure3dVersion(Secure3dVersion.ANY);
        ServicesContainer.configureService(config);

        // create card data
        card = new CreditCardData();
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
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());

        if (secureEcom.isEnrolled()) {
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

            if(secureEcom.getStatus().equals("Y")) {
                Transaction response = card.charge().execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
    }

    @Test
    public void fullCycle_Any() throws ApiException {
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .withAmount(new BigDecimal(1))
                .withCurrency("USD")
                .execute(Secure3dVersion.ANY);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            if(secureEcom.getVersion().equals(Secure3dVersion.TWO)) {
                assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

                // initiate authentication
                ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                        .withAmount(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .withOrderCreateDate(DateTime.now())
                        .withAddress(billingAddress, AddressType.Billing)
                        .withAddress(shippingAddress, AddressType.Shipping)
                        .withBrowserData(browserData)
                        .withMethodUrlCompletion(MethodUrlCompletion.No)
                        .execute();
                assertNotNull(initAuth);

                // get authentication data
                secureEcom = Secure3dService.getAuthenticationData()
                        .withServerTransactionId(initAuth.getServerTransactionId())
                        .execute();
                card.setThreeDSecure(secureEcom);

                if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                    Transaction response = card.charge(new BigDecimal("10.01"))
                            .withCurrency("USD")
                            .execute();
                    assertNotNull(response);
                    assertEquals("00", response.getResponseCode());
                }
                else fail("Signature verification failed.");
            }
            else {
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

                if(secureEcom.getStatus().equals("Y")) {
                    Transaction response = card.charge().execute();
                    assertNotNull(response);
                    assertEquals("00", response.getResponseCode());
                }
                else fail("Signature verification failed.");
            }
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = stored.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.verify()
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = stored.verify()
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("10.01"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withMerchantInitiatedRequestType(AuthenticationRequestType.RecurringTransaction)

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withGiftCardCount(1)
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

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
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

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
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
                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withMaxNumberOfInstallments(5)
                    .withRecurringAuthorizationFrequency(25)
                    .withRecurringAuthorizationExpiryDate(DateTime.parse("2019-08-25"))

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
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
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

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

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
    }

    @Test
    public void optionalMobileFields() throws ApiException {
        // check enrollment
        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.TWO);
        assertNotNull(secureEcom);

        if (secureEcom.isEnrolled()) {
            assertEquals(Secure3dVersion.TWO, secureEcom.getVersion());

            // initiate authentication
            ThreeDSecure initAuth = Secure3dService.initiateAuthentication(card, secureEcom)
                    .withAmount(new BigDecimal("250.00"))
                    .withCurrency("USD")
                    .withOrderCreateDate(DateTime.now())
                    .withAddress(billingAddress, AddressType.Billing)
                    .withAddress(shippingAddress, AddressType.Shipping)
                    .withBrowserData(browserData)
                    .withMethodUrlCompletion(MethodUrlCompletion.No)

                    // optionals
                    .withApplicationId("f283b3ec-27da-42a1-acea-f3f70e75bbdc")
                    .withSdkInterface(SdkInterface.Both)
                    .withSdkUiTypes(SdkUiType.Text, SdkUiType.SingleSelect, SdkUiType.MultiSelect, SdkUiType.OOB, SdkUiType.HTML_Other)
//                    .withEphemeralPublicKey("{\"kty\":\"EC\",\"crv\":\"p-256\",\"x\":\"WWcpTjbOqiu_1aODllw5rYTq5oLXE_T0huCPjMIRbkI\",\"y\":\"Wz_7anIeadV8SJZUfr4drwjzuWoUbOsHp5GdRZBAAiw\"}")
//                    .withMaximumTimeout(5)
                    .withReferenceNumber("3DS_LOA_SDK_PPFU_020100_00007")
                    .withSdkTransactionId("b2385523-a66c-4907-ac3c-91848e8c0067")
                    .withEncodedData("ew0KCSJEViI6ICIxLjAiLA0KCSJERCI6IHsNCgkJIkMwMDEiOiAiQW5kcm9pZCIsDQoJCSJDMDAyIjogIkhUQyBPbmVfTTgiLA0KCQkiQzAwNCI6ICI1LjAuMSIsDQoJCSJDMDA1IjogImVuX1VTIiwNCgkJIkMwMDYiOiAiRWFzdGVybiBTdGFuZGFyZCBUaW1lIiwNCgkJIkMwMDciOiAiMDY3OTc5MDMtZmI2MS00MWVkLTk0YzItNGQyYjc0ZTI3ZDE4IiwNCgkJIkMwMDkiOiAiSm9obidzIEFuZHJvaWQgRGV2aWNlIg0KCX0sDQoJIkRQTkEiOiB7DQoJCSJDMDEwIjogIlJFMDEiLA0KCQkiQzAxMSI6ICJSRTAzIg0KCX0sDQoJIlNXIjogWyJTVzAxIiwgIlNXMDQiXQ0KfQ0K")

                    .execute();
            assertNotNull(initAuth);

            // get authentication data
            secureEcom = Secure3dService.getAuthenticationData()
                    .withServerTransactionId(initAuth.getServerTransactionId())
                    .execute();
            card.setThreeDSecure(secureEcom);

            if(secureEcom.getStatus().equals("AUTHENTICATION_SUCCESSFUL")) {
                Transaction response = card.charge(new BigDecimal("10.01"))
                        .withCurrency("USD")
                        .execute();
                assertNotNull(response);
                assertEquals("00", response.getResponseCode());
            }
            else fail("Signature verification failed.");
        }
        else fail("Card not enrolled.");
    }

    @Test
    public void checkVersion_Not_Enrolled() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4012001037141112");
        card.setExpMonth(12);
        card.setExpYear(2025);

        ThreeDSecure secureEcom = Secure3dService.checkEnrollment(card)
                .execute(Secure3dVersion.ANY);
        assertNotNull(secureEcom);
        assertFalse(secureEcom.isEnrolled());
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
                "e193c21a-ce64-4820-b5b6-8f46715de931",
                "10c3e089-fa98-4352-bc4e-4b37f7dcf108"
        );
        paymentMethod.setThreeDSecure(threeDSecureData);

        Transaction response = paymentMethod.charge(new BigDecimal("10.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
