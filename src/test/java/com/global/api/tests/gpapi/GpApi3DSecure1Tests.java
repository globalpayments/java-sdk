package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.Secure3dService;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class GpApi3DSecure1Tests extends BaseGpApiTest {

    private final static String SUCCESS_AUTHENTICATED = "SUCCESS_AUTHENTICATED";
    private final static String CHALLENGE_REQUIRED = "CHALLENGE_REQUIRED";
    private final static String ENROLLED = "ENROLLED";
    private final static String NOT_ENROLLED = "NOT_ENROLLED";

    private final BigDecimal amount = new BigDecimal("10.01");
    private final String currency = "GBP";

    private CreditCardData card;

    public GpApi3DSecure1Tests() throws ConfigurationException {
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

}