package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Collection;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

// TODO: @alexandru: Unify naming convention with other SDKs

@RunWith(Enclosed.class)
public class GpApi3DSecureTestsParameterized extends BaseGpApiTest {

    private final static String AVAILABLE = "AVAILABLE";
    private final static String ENROLLED = "ENROLLED";
    private final static String AUTHENTICATION_COULD_NOT_BE_PERFORMED = "AUTHENTICATION_COULD_NOT_BE_PERFORMED";
    private final static String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    private final static String NOT_AUTHENTICATED = "NOT_AUTHENTICATED";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String SUCCESS = "SUCCESS";
    private final static String SUCCESS_AUTHENTICATED = "SUCCESS_AUTHENTICATED";
    private final static String FAILED = "FAILED";
    private final static String SUCCESS_ATTEMPT_MADE = "SUCCESS_ATTEMPT_MADE";

    private static final BigDecimal amount = new BigDecimal("10.01");
    private static final String currency = "GBP";

    private static CreditCardData card;
    private static Address shippingAddress;
    private static BrowserData browserData;

    abstract public static class SharedSetUp {
        @BeforeClass
        @SneakyThrows
        public static void init() {
            GpApiConfig config = new GpApiConfig();

            // GP-API settings
            config.setAppId(APP_ID);
            config.setAppKey(APP_KEY);
            config.setCountry("GB");
            config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
            config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
            config.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");

            config.setEnableLogging(true);

            ServicesContainer.configureService(config);

            card = new CreditCardData();
            card.setExpMonth(12);
            card.setExpYear(2025);
            card.setCardHolderName("John Smith");

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
        }
    }

    @RunWith(Parameterized.class)
    public static class Successful3DSV2CardTests extends SharedSetUp {
        @Parameterized.Parameters(name = "Successful3DSCardTests :: {index} :: card [{0}], status [{1}]")
        public static Collection input() {
            return asList(new Object[][]{
                    {CARD_AUTH_SUCCESSFUL_V2_1.cardNumber, SUCCESS_AUTHENTICATED},
                    {CARD_AUTH_SUCCESSFUL_NO_METHOD_URL_V2_1.cardNumber, SUCCESS_AUTHENTICATED},
                    {CARD_AUTH_SUCCESSFUL_V2_2.cardNumber, SUCCESS_AUTHENTICATED},
                    {CARD_AUTH_SUCCESSFUL_NO_METHOD_URL_V2_2.cardNumber, SUCCESS_AUTHENTICATED}
            });
        }

        @Parameterized.Parameter(0)
        public String cardNumber;
        @Parameterized.Parameter(1)
        public String status;

        @Test
        public void Frictionless_AuthenticationSuccessful_FullCycle_v2() throws ApiException {
            card.setNumber(cardNumber);

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
                            .execute();

            assertNotNull(initAuth);
            assertEquals(SUCCESS_AUTHENTICATED, initAuth.getStatus());
            assertEquals("YES", initAuth.getLiabilityShift());

            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute();

            assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
            assertEquals("YES", secureEcom.getLiabilityShift());

            card.setThreeDSecure(secureEcom);

            Transaction response =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(response);
            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        }
    }

    @RunWith(Parameterized.class)
    public static class Failed3DSV2CardTests extends SharedSetUp {
        @Parameterized.Parameters(name = "Failed3DSCardTests :: {index} :: card [{0}], status [{1}]")
        public static Collection input() {
            return asList(new Object[][]{
                    {CARD_AUTH_ATTEMPTED_BUT_NOT_SUCCESSFUL_V2_1.cardNumber, SUCCESS_ATTEMPT_MADE},
                    {CARD_AUTH_FAILED_V2_1.cardNumber, NOT_AUTHENTICATED},
                    {CARD_AUTH_ISSUER_REJECTED_V2_1.cardNumber, FAILED},
                    {CARD_AUTH_COULD_NOT_BE_PREFORMED_V2_1.cardNumber, FAILED},
                    {CARD_AUTH_ATTEMPTED_BUT_NOT_SUCCESSFUL_V2_2.cardNumber, SUCCESS_ATTEMPT_MADE},
                    {CARD_AUTH_FAILED_V2_2.cardNumber, NOT_AUTHENTICATED},
                    {CARD_AUTH_ISSUER_REJECTED_V2_2.cardNumber, FAILED},
                    {CARD_AUTH_COULD_NOT_BE_PREFORMED_V2_2.cardNumber, FAILED}
            });
        }

        @Parameterized.Parameter(0)
        public String cardNumber;
        @Parameterized.Parameter(1)
        public String status;

        @Test
        public void Frictionless_AuthenticationFailed_FullCycle_v2() throws ApiException {
            card.setNumber(cardNumber);

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
                            .execute();

            assertNotNull(initAuth);
            assertEquals(status, initAuth.getStatus());
            assertTrue(initAuth.getEci().equals("06") || initAuth.getEci().equals("07"));

            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute();

            assertEquals(status, secureEcom.getStatus());
            assertFalse(secureEcom.isChallengeMandated());
//        assertEquals("06", secureEcom.getEci());

            card.setThreeDSecure(secureEcom);

            Transaction response =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(response);
            assertEquals(SUCCESS, response.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        }
    }

    @RunWith(Parameterized.class)
    public static class ChallengeRequiredFailed3DSV1CardTests extends SharedSetUp {
        @Parameterized.Parameters(name = "ChallengeRequiredFailed3DSV1CardTests :: {index} :: card [{0}], status [{1}], acs_status [{2}]")
        public static Collection input() {
            return asList(new Object[][]{
                    {CARDHOLDER_ENROLLED_V1.cardNumber, FAILED, GpApi3DSecureTests.AuthenticationResultCode.Unavailable},
                    {CARDHOLDER_ENROLLED_V1.cardNumber, NOT_AUTHENTICATED, GpApi3DSecureTests.AuthenticationResultCode.Failed},
                    {CARDHOLDER_ENROLLED_V1.cardNumber, SUCCESS_ATTEMPT_MADE, GpApi3DSecureTests.AuthenticationResultCode.AttemptAcknowledge}
            });
        }

        @Parameterized.Parameter(0)
        public String cardNumber;
        @Parameterized.Parameter(1)
        public String status;
        @Parameterized.Parameter(2)
        public GpApi3DSecureTests.AuthenticationResultCode AcsStatus;

        @Test
        public void cardHolderEnrolled_ChallengeRequired_AuthenticationUnavailable_v1() throws Exception {
            card.setNumber(cardNumber);

            ThreeDSecure secureEcom =
                    Secure3dService
                            .checkEnrollment(card)
                            .withCurrency(currency)
                            .withAmount(amount)
                            .execute();

            assertNotNull(secureEcom);
            assertEquals(ENROLLED, secureEcom.getEnrolledStatus());
            assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
            assertEquals(CHALLENGE_REQUIRED, secureEcom.getStatus());
            assertTrue(secureEcom.isChallengeMandated());
            assertNotNull(secureEcom.getIssuerAcsUrl());
            assertNotNull(secureEcom.getPayerAuthenticationRequest());

            GpApi3DSecureTests.GpApi3DSecureAcsClient acsClient = new GpApi3DSecureTests.GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
            StringBuffer payerAuthenticationResponse = new StringBuffer("");
            String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, AcsStatus);
            assertEquals("{\"success\":true}", authResponse);

            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(secureEcom.getServerTransactionId())
                            .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                            .execute();

            assertNotNull(secureEcom);
            // status is coming empty in the response of 3rd parametrized test
            assertEquals(status, secureEcom.getStatus());
        }
    }

    @RunWith(Parameterized.class)
    public static class ChallengeRequired3DSV2CardTests extends SharedSetUp {
        @Parameterized.Parameters(name = "ChallengeRequired3DSV2CardTests :: {index} :: card [{0}]")
        public static Collection input() {
            return asList(new Object[][]{
                    {CARD_CHALLENGE_REQUIRED_V2_1.cardNumber},
                    {CARD_CHALLENGE_REQUIRED_V2_2.cardNumber}
            });
        }

        @Parameterized.Parameter(0)
        public String cardNumber;

        @Test
        public void CardHolderEnrolled_ChallengeRequired_v2() throws Exception {
            card.setNumber(cardNumber);

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
                            .execute();

            assertNotNull(initAuth);
            assertEquals(CHALLENGE_REQUIRED, initAuth.getStatus());
            assertTrue(initAuth.isChallengeMandated());
            assertNotNull(initAuth.getIssuerAcsUrl());
            assertNotNull(initAuth.getPayerAuthenticationRequest());

            GpApi3DSecureTests.GpApi3DSecureAcsClient acsClient = new GpApi3DSecureTests.GpApi3DSecureAcsClient(initAuth.getIssuerAcsUrl());
            String authResponse = acsClient.authenticate_v2(initAuth);
            assertEquals("{\"success\":true}", authResponse);

            secureEcom =
                    Secure3dService
                            .getAuthenticationData()
                            .withServerTransactionId(initAuth.getServerTransactionId())
                            .execute();

            assertNotNull(secureEcom);
            assertEquals(SUCCESS_AUTHENTICATED, secureEcom.getStatus());
            assertEquals("YES", secureEcom.getLiabilityShift());

            card.setThreeDSecure(secureEcom);

            Transaction response =
                    card
                            .charge(amount)
                            .withCurrency(currency)
                            .execute();

            assertNotNull(response);
            assertEquals(SUCCESS, response.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
        }
    }

}