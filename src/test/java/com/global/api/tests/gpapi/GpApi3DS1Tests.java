package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.StoredCredential;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.global.api.tests.gpapi.GpApi3DSTestCards.CARDHOLDER_ENROLLED_V1;
import static com.global.api.tests.gpapi.GpApi3DSTestCards.CARDHOLDER_NOT_ENROLLED_V1;
import static org.junit.Assert.*;

public class GpApi3DS1Tests extends BaseGpApiTest {

    private final static String AUTHENTICATION_SUCCESSFUL = "AUTHENTICATION_SUCCESSFUL";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String NOT_ENROLLED = "NOT_ENROLLED";

    private final BigDecimal amount = new BigDecimal("10.01");
    private final String currency = "GBP";

    private CreditCardData card;

    public GpApi3DS1Tests() throws ConfigurationException {
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
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardHolderName("John Smith");
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1() throws ApiException {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .withIdempotencyKey(idempotencyKey)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .checkEnrollment(card)
                    .withCurrency(currency)
                    .withAmount(amount)
                    .withIdempotencyKey(idempotencyKey)
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_TokenizedCard() throws ApiException {
        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(card.tokenize(GP_API_CONFIG_NAME));

        assertNotNull(tokenizedCard.getToken());

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(tokenizedCard)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_AllPreferenceValues() throws ApiException {
        for (ChallengeRequestIndicator challengeRequestIndicator : ChallengeRequestIndicator.values()) {
            ThreeDSecure secureEcom =
                    Secure3dService
                            .checkEnrollment(card)
                            .withCurrency(currency)
                            .withChallengeRequestIndicator(challengeRequestIndicator)
                            .withAmount(amount)
                            .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

            assertThreeDSResponse(secureEcom);
        }
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_StoredCredentials() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);
        storedCredential.setType(StoredCredentialType.Subscription);
        storedCredential.setSequence(StoredCredentialSequence.Subsequent);
        storedCredential.setReason(StoredCredentialReason.Incremental);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withStoredCredential(storedCredential)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);
    }

    //TODO - update for transaction type refund to be sent
    @Ignore
    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_Refund() throws ApiException {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withTransactionType(TransactionType.Refund)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_AllSources() throws ApiException {
        for (AuthenticationSource source : AuthenticationSource.values()) {
            ThreeDSecure secureEcom =
                    Secure3dService
                            .checkEnrollment(card)
                            .withCurrency(currency)
                            .withAmount(amount)
                            .withAuthenticationSource(source)
                            .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

            assertThreeDSResponse(secureEcom);
        }
    }

    @Test
    public void testCardHolderEnrolled_ChallengeRequired_v1_WithNullPaymentMethod() throws ApiException {
        card = new CreditCardData();

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .checkEnrollment(card)
                    .withCurrency(currency)
                    .withAmount(amount)
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40007", ex.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following conditionally mandatory fields number,expiry_month,expiry_year.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void testCardHolderNotEnrolled_v1() throws ApiException {
        card.setNumber(CARDHOLDER_NOT_ENROLLED_V1.cardNumber);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(NOT_ENROLLED, secureEcom.getStatus());
        assertFalse(secureEcom.isChallengeMandated());
        // assertEquals("6", secureEcom.getEci());
    }

    /**
     * Tests for 3DS v1 Card Enrolled - Obtain Result
     */
    @Test
    @SneakyThrows
    public void testCardHolderEnrolled_V1_PostResult() {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);

        // Perform ACS authentication
        GpApi3DSecureTests.GpApi3DSecureAcsClient acsClient = new GpApi3DSecureTests.GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, null);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        ThreeDSecure postAuthResultSecureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(postAuthResultSecureEcom);
        assertEquals(Secure3dVersion.ONE, postAuthResultSecureEcom.getVersion());
        assertEquals(AUTHENTICATION_SUCCESSFUL, postAuthResultSecureEcom.getStatus());
        assertTrue(postAuthResultSecureEcom.isChallengeMandated());
        assertNotNull(postAuthResultSecureEcom.getIssuerAcsUrl());
        assertNotNull(postAuthResultSecureEcom.getPayerAuthenticationRequest());
    }

    @Test
    public void testCardHolderEnrolled_V1_PostResult_WithIdempotencyKey() throws ApiException {
        String idempotencyKey = UUID.randomUUID().toString();

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);
        assertTrue(secureEcom.isChallengeMandated());

        // Perform ACS authentication
        GpApi3DSecureTests.GpApi3DSecureAcsClient acsClient = new GpApi3DSecureTests.GpApi3DSecureAcsClient(secureEcom.getIssuerAcsUrl());
        StringBuffer payerAuthenticationResponse = new StringBuffer("");
        String authResponse = acsClient.authenticate_v1(secureEcom, payerAuthenticationResponse, null);
        assertEquals("{\"success\":true}", authResponse);

        // Get authentication data
        ThreeDSecure postAuthResultSecureEcom =
                Secure3dService
                        .getAuthenticationData()
                        .withServerTransactionId(secureEcom.getServerTransactionId())
                        .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                        .withIdempotencyKey(idempotencyKey)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(postAuthResultSecureEcom);
        assertEquals(Secure3dVersion.ONE, postAuthResultSecureEcom.getVersion());
        assertEquals(AUTHENTICATION_SUCCESSFUL, postAuthResultSecureEcom.getStatus());
        assertTrue(postAuthResultSecureEcom.isChallengeMandated());
        assertNotNull(postAuthResultSecureEcom.getIssuerAcsUrl());
        assertNotNull(postAuthResultSecureEcom.getPayerAuthenticationRequest());

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .withServerTransactionId(secureEcom.getServerTransactionId())
                    .withPayerAuthenticationResponse(payerAuthenticationResponse.toString())
                    .withIdempotencyKey(idempotencyKey)
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertTrue(ex.getMessage().startsWith("Status Code: 409 - Idempotency Key seen before:"));
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    @SneakyThrows
    public void testCardHolderEnrolled_V1_PostResult_NonExistentId() {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .withServerTransactionId(secureEcom.getServerTransactionId())
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50027", ex.getResponseText());
            assertEquals("Status Code: 400 - Undefined element in Message before PARes", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void testCardHolderNotEnrolled_v1_PostResult() throws ApiException {
        card.setNumber(CARDHOLDER_NOT_ENROLLED_V1.cardNumber);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertNotNull(secureEcom);
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(NOT_ENROLLED, secureEcom.getStatus());
        assertFalse(secureEcom.isChallengeMandated());
//        assertEquals("6", secureEcom.getEci());

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .withServerTransactionId(secureEcom.getServerTransactionId())
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50027", ex.getResponseText());
            assertEquals("Status Code: 400 - Undefined element in Message before PARes", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void testCardHolderEnrolled_V1_PostResult_AcsNotPerformed() throws ApiException {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom);

        boolean exceptionCaught = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .withServerTransactionId(secureEcom.getServerTransactionId())
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("50027", ex.getResponseText());
            assertEquals("Status Code: 400 - Undefined element in Message before PARes", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private void assertThreeDSResponse(ThreeDSecure secureEcom) {
        assertNotNull(secureEcom);
//         assertTrue(secureEcom.isEnrolled());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(GpApi3DS1Tests.CHALLENGE_REQUIRED, secureEcom.getStatus());
        assertTrue(secureEcom.isChallengeMandated());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getPayerAuthenticationRequest());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
    }
}