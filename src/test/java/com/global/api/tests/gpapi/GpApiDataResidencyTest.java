package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.DataResidency;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static com.global.api.entities.enums.ServiceEndpoints.*;
import static org.junit.jupiter.api.Assertions.*;

class GpApiDataResidencyTest extends BaseGpApiTest {
    private final BigDecimal amount = new BigDecimal("12.02");
    private final String currency = "USD";
    GpApiConfig config;


    public GpApiDataResidencyTest() throws ApiException {
        config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setEnableLogging(true);
        config.setEnvironment(Environment.TEST);
        config.setRequestLogger(new RequestConsoleLogger());
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);

    }

    @Test
    void testDataResidencyEu() throws ApiException {

        GpApiConfig config = gpApiSetup(EU_APP_ID, EU_APP_KEY, Channel.CardNotPresent);
        config.setDataResidency(DataResidency.EU);
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        config.setEnvironment(Environment.TEST);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("internet");
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);

        assertEquals(DataResidency.EU, config.getDataResidency());
        assertEquals(GP_API_TEST_EU.getValue(), config.getServiceUrl());

        CreditCardData card = getMasterCardData();

        Transaction response = card
                .charge(amount)
                .withCurrency("EUR")
                .execute();
        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    @Test
    void testDataResidencyDefaultsToNone() throws ApiException {

        assertEquals(DataResidency.NONE, config.getDataResidency());
        assertEquals(GP_API_TEST.getValue(), config.getServiceUrl());

        CreditCardData card = getMasterCardData();
        Transaction response = card
                .charge(amount)
                .withCurrency(currency)
                .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
    }

    //This test case will fail as QA endpoint is not yet set up to handle EU data residency. This is expected and will be resolved once the endpoint is ready.
    // The purpose of this test is to ensure that the correct endpoint is being hit for EU data residency in QA environment, so we just need to verify the endpoint in log section.
    @Test
    void testDataResidencyEuForQa() throws ApiException {

        GpApiConfig config = gpApiSetup(EU_APP_ID, EU_APP_KEY, Channel.CardNotPresent);
        config.setDataResidency(DataResidency.EU);
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        config.setEnvironment(Environment.QA);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("internet");
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);

        assertEquals(DataResidency.EU, config.getDataResidency());
        assertEquals(GP_API_QA_EU.getValue(), config.getServiceUrl());

        CreditCardData card = getMasterCardData();
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .execute();
        });

    }

    // The purpose of this test is to ensure that the correct endpoint is being hit for EU data residency in Production environment, so we just need to verify the endpoint in log section.
    @Test
    void testDataResidencyEuForProduction() throws ApiException {

        GpApiConfig config = gpApiSetup(EU_APP_ID, EU_APP_KEY, Channel.CardNotPresent);
        config.setDataResidency(DataResidency.EU);
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        config.setEnvironment(Environment.PRODUCTION);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("internet");
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);

        assertEquals(DataResidency.EU, config.getDataResidency());
        assertEquals(GP_API_PRODUCTION_EU.getValue(), config.getServiceUrl());

        CreditCardData card = getMasterCardData();
        ApiException exception = assertThrows(GatewayException.class, () -> {
            card
                    .charge(amount)
                    .withCurrency(currency)
                    .execute();

        });
    }

    private static CreditCardData getMasterCardData() {
        CreditCardData card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(05);
        card.setExpYear(2026);
        card.setCvn("852");
        card.setCardPresent(false);
        card.setReaderPresent(false);
        return card;
    }

}
