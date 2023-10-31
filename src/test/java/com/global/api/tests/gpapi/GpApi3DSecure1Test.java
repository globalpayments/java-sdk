package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARDHOLDER_ENROLLED_V1;
import static com.global.api.tests.gpapi.BaseGpApiTest.GpApi3DSTestCards.CARDHOLDER_NOT_ENROLLED_V1;
import static org.junit.Assert.*;

public class GpApi3DSecure1Test extends BaseGpApiTest {

    private final static String NOT_ENROLLED = "NOT_ENROLLED";
    private final BigDecimal amount = new BigDecimal("3.71");
    private final String currency = "GBP";

    private CreditCardData card;

    public GpApi3DSecure1Test() throws ConfigurationException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);

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
                    .execute(Secure3dVersion.ONE);
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
                        .execute();

        assertThreeDSResponse(secureEcom);
    }

    @Test
    public void CardHolderNotEnrolled_v1() throws ApiException {
        card.setNumber(CARDHOLDER_NOT_ENROLLED_V1.cardNumber);

        ThreeDSecure secureEcom =
                Secure3dService
                        .checkEnrollment(card)
                        .withCurrency(currency)
                        .withAmount(amount)
                        .execute();

        assertThreeDSResponse(secureEcom);
    }

    @Test
    public void GetAuthenticationData_V1() throws ApiException {
        boolean errorFound = false;
        try {
            Secure3dService
                    .getAuthenticationData()
                    .execute(Secure3dVersion.ONE);
        } catch (BuilderException e) {
            errorFound = true;
            assertEquals("3D Secure ONE is no longer supported!", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    private void assertThreeDSResponse(ThreeDSecure secureEcom) {
        assertNotNull(secureEcom);
        assertEquals(NOT_ENROLLED, secureEcom.getEnrolledStatus());
        assertEquals(NOT_ENROLLED, secureEcom.getStatus());
        assertNotNull(secureEcom.getIssuerAcsUrl());
        assertNotNull(secureEcom.getChallengeReturnUrl());
        assertNotNull(secureEcom.getSessionDataFieldName());
        assertNotNull(secureEcom.getMessageType());
        assertNull(secureEcom.getEci());
        assertEquals("NO", secureEcom.getLiabilityShift());
        assertFalse(secureEcom.isEnrolled());
    }

}