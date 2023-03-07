package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARDHOLDER_ENROLLED_V1;
import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARDHOLDER_NOT_ENROLLED_V1;
import static org.junit.Assert.*;

public class GpApi3DSecure1Test extends BaseGpApiTest {

    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String ENROLLED = "ENROLLED";
    private final static String NOT_ENROLLED = "NOT_ENROLLED";

    private final BigDecimal amount = new BigDecimal("10.01");
    private final String currency = "GBP";

    private CreditCardData card;

    public GpApi3DSecure1Test() throws ConfigurationException {
        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config.setAppId(APP_ID);
        config.setAppKey(APP_KEY);
        config.setCountry("GB");
        config.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        config.setMerchantContactUrl("'https://enp4qhvjseljg.x.pipedream.net/");

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);

        // Create card data
        card = new CreditCardData();
        card.setNumber(CARDHOLDER_ENROLLED_V1.cardNumber);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCardHolderName("John Smith");
    }

    @Test
    public void CheckEnrollment_V1() throws ApiException {
        boolean errorFound = false;
        try {
            Secure3dService
                    .checkEnrollment(new CreditCardData())
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (BuilderException e) {
            errorFound = true;
            assertEquals("3D Secure ONE is no longer supported!", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void CardHolderEnrolled_v1() throws ApiException {
        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom, ENROLLED, CHALLENGE_REQUIRED);
        assertTrue(secureEcom.isChallengeMandated());
    }

    @Test
    public void CardHolderNotEnrolled_v1() throws ApiException {
        card.setNumber(CARDHOLDER_NOT_ENROLLED_V1.cardNumber);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute(GP_API_CONFIG_NAME);

        assertThreeDSResponse(secureEcom, NOT_ENROLLED, NOT_ENROLLED);
    }

    @Ignore
    @Test
    public void CardHolderEnrolled_v1_ConfigException() throws ApiException {
        boolean errorFound = false;
        try {
            Secure3dService
                    .checkEnrollment(card)
                    .withCurrency(currency)
                    .withAmount(amount)
                    .execute();
        } catch (ConfigurationException e) {
            errorFound = true;
            assertEquals("Secure 3d is not configured on the connector", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void GetAuthenticationData_V1() throws ApiException {
        boolean errorFound = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .execute(Secure3dVersion.ONE, GP_API_CONFIG_NAME);
        } catch (BuilderException e) {
            errorFound = true;
            assertEquals("3D Secure ONE is no longer supported!", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    private void assertThreeDSResponse(ThreeDSecure secureEcom, String status, String enrolled) {
        assertNotNull(secureEcom);
        assertEquals(status, secureEcom.getEnrolledStatus());
        assertEquals(Secure3dVersion.ONE, secureEcom.getVersion());
        assertEquals(enrolled, secureEcom.getStatus());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertEquals("1.0.0", secureEcom.getMessageVersion());
        assertNull(secureEcom.getEci());
        assertEquals("NO", secureEcom.getLiabilityShift());
    }

}