package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.gpApi.entities.PorticoTokenConfig;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.joda.time.DateTime;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/// <summary>
/// Test class for verifying CreditSale transactions using different Portico, SecretApiKey, and GpApi credentials/configurations.
/// </summary>
public class GpApiCreateTokenWithPorticoCredentialTests extends BaseGpApiTest {
    private CreditCardData masterCard;
    private CreditCardData visaCard;
    static final int expMonth = DateTime.now().getMonthOfYear();
    static final int expYear = DateTime.now().getYear() + 1;

    @BeforeEach
    public void testInitialize() throws ConfigurationException {
        // Register service config using "Legacy" Portico credentials
        GpApiConfig legacyPorticoConfig = gpApiSetup("", "", Channel.CardNotPresent);
        legacyPorticoConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        legacyPorticoConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        legacyPorticoConfig.setAccessTokenInfo(accessTokenInfo);
        PorticoTokenConfig legacyPorticoTokenConfig = new PorticoTokenConfig();
        legacyPorticoTokenConfig.setDeviceId(11753);
        legacyPorticoTokenConfig.setSiteId(418948);
        legacyPorticoTokenConfig.setLicenseId(388244);
        legacyPorticoTokenConfig.setUsername("gateway1213846");
        legacyPorticoTokenConfig.setPassword("$Test1234");
        legacyPorticoConfig.setPorticoTokenConfig(legacyPorticoTokenConfig);
        ServicesContainer.configureService(legacyPorticoConfig, "LegacyPorticoConfig");

        // Register service config using SecretApiKey only
        GpApiConfig secretApiKeyConfig = gpApiSetup("", "", Channel.CardNotPresent);
        secretApiKeyConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        secretApiKeyConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo2 = new AccessTokenInfo();
        accessTokenInfo2.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        secretApiKeyConfig.setAccessTokenInfo(accessTokenInfo2);
        PorticoTokenConfig secretApiKeyTokenConfig = new PorticoTokenConfig();
        secretApiKeyTokenConfig.setSecretApiKey("skapi_cert_MVISAgC05V8Amnxg2jARLKW-K4ONQeXejrWYCCA_Cw");
        secretApiKeyConfig.setPorticoTokenConfig(secretApiKeyTokenConfig);
        ServicesContainer.configureService(secretApiKeyConfig, "SecretApiKeyConfig");

        // Register service config with both Portico credentials and SecretApiKey
        GpApiConfig fullPorticoConfig = gpApiSetup("", "", Channel.CardNotPresent);
        fullPorticoConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        fullPorticoConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo3 = new AccessTokenInfo();
        accessTokenInfo3.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        fullPorticoConfig.setAccessTokenInfo(accessTokenInfo3);
        PorticoTokenConfig fullPorticoTokenConfig = new PorticoTokenConfig();
        fullPorticoTokenConfig.setDeviceId(11753);
        fullPorticoTokenConfig.setSiteId(418948);
        fullPorticoTokenConfig.setLicenseId(388244);
        fullPorticoTokenConfig.setUsername("gateway1213846");
        fullPorticoTokenConfig.setPassword("$Test1234");
        fullPorticoTokenConfig.setSecretApiKey("skapi_cert_MVISAgC05V8Amnxg2jARLKW-K4ONQeXejrWYCCA_Cw");
        fullPorticoConfig.setPorticoTokenConfig(fullPorticoTokenConfig);
        ServicesContainer.configureService(fullPorticoConfig, "FullPorticoConfig");

        // Register legacy config with AppId included
        GpApiConfig legacyPorticoAppIdConfig = gpApiSetup("", "", Channel.CardNotPresent);
        legacyPorticoAppIdConfig.setAppId("jYtVGox8yvG6KQwlNHPxbfyDa13kwOGt");
        legacyPorticoAppIdConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        legacyPorticoAppIdConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo4 = new AccessTokenInfo();
        accessTokenInfo4.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        legacyPorticoAppIdConfig.setAccessTokenInfo(accessTokenInfo4);
        PorticoTokenConfig legacyPorticoAppIdTokenConfig = new PorticoTokenConfig();
        legacyPorticoAppIdTokenConfig.setDeviceId(11753);
        legacyPorticoAppIdTokenConfig.setSiteId(418948);
        legacyPorticoAppIdTokenConfig.setLicenseId(388244);
        legacyPorticoAppIdTokenConfig.setUsername("gateway1213846");
        legacyPorticoAppIdTokenConfig.setPassword("$Test1234");
        legacyPorticoAppIdConfig.setPorticoTokenConfig(legacyPorticoAppIdTokenConfig);
        ServicesContainer.configureService(legacyPorticoAppIdConfig, "LegacyPorticoAppIdConfig");

        // Register config with SecretApiKey and AppId
        GpApiConfig secretApiKeyAppIdConfig = gpApiSetup("", "", Channel.CardNotPresent);
        secretApiKeyAppIdConfig.setAppId("jYtVGox8yvG6KQwlNHPxbfyDa13kwOGt");
        secretApiKeyAppIdConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        secretApiKeyAppIdConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo5 = new AccessTokenInfo();
        accessTokenInfo5.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        secretApiKeyAppIdConfig.setAccessTokenInfo(accessTokenInfo5);
        PorticoTokenConfig secretApiKeyAppIdTokenConfig = new PorticoTokenConfig();
        secretApiKeyAppIdTokenConfig.setSecretApiKey("skapi_cert_MVISAgC05V8Amnxg2jARLKW-K4ONQeXejrWYCCA_Cw");
        secretApiKeyAppIdConfig.setPorticoTokenConfig(secretApiKeyAppIdTokenConfig);
        ServicesContainer.configureService(secretApiKeyAppIdConfig, "SecretApiKeyAppIdConfig");

        // Register config with Portico credentials, SecretApiKey, and AppId
        GpApiConfig fullPorticoAppIdConfig = gpApiSetup("", "", Channel.CardNotPresent);
        fullPorticoAppIdConfig.setAppId("jYtVGox8yvG6KQwlNHPxbfyDa13kwOGt");
        fullPorticoAppIdConfig.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        fullPorticoAppIdConfig.setCountry("US");
        AccessTokenInfo accessTokenInfo6 = new AccessTokenInfo();
        accessTokenInfo6.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        fullPorticoAppIdConfig.setAccessTokenInfo(accessTokenInfo6);
        PorticoTokenConfig fullPorticoAppIdTokenConfig = new PorticoTokenConfig();
        fullPorticoAppIdTokenConfig.setDeviceId(11753);
        fullPorticoAppIdTokenConfig.setSiteId(418948);
        fullPorticoAppIdTokenConfig.setLicenseId(388244);
        fullPorticoAppIdTokenConfig.setUsername("gateway1213846");
        fullPorticoAppIdTokenConfig.setPassword("$Test1234");
        fullPorticoAppIdTokenConfig.setSecretApiKey("skapi_cert_MVISAgC05V8Amnxg2jARLKW-K4ONQeXejrWYCCA_Cw");
        fullPorticoAppIdConfig.setPorticoTokenConfig(fullPorticoAppIdTokenConfig);
        ServicesContainer.configureService(fullPorticoAppIdConfig, "FullPorticoAppIdConfig");

        // Register a config with intentionally invalid credentials for negative test scenarios.
        GpApiConfig gpApiConfigFailingScenarios = gpApiSetup("", "", Channel.CardNotPresent);
        gpApiConfigFailingScenarios.setAppId("jYtVGox8yvG6KQwlNHPxbfyDa13kwOGt");
        gpApiConfigFailingScenarios.setServiceUrl("https://apis-qa.globalpay.com/ucp");
        gpApiConfigFailingScenarios.setCountry("US");
        AccessTokenInfo accessTokenInfoFail = new AccessTokenInfo();
        accessTokenInfoFail.setTransactionProcessingAccountName("accessTokenValidationsecretKey");
        gpApiConfigFailingScenarios.setAccessTokenInfo(accessTokenInfoFail);
        PorticoTokenConfig porticoTokenConfigFail = new PorticoTokenConfig();
        porticoTokenConfigFail.setDeviceId(11753);
        porticoTokenConfigFail.setSiteId(418948);
        porticoTokenConfigFail.setLicenseId(388244);
        porticoTokenConfigFail.setUsername(""); // Intentionally left blank for failure scenario
        porticoTokenConfigFail.setPassword(""); // Intentionally left blank for failure scenario
        porticoTokenConfigFail.setSecretApiKey(""); // Intentionally left blank for failure scenario
        gpApiConfigFailingScenarios.setPorticoTokenConfig(porticoTokenConfigFail);
        ServicesContainer.configureService(gpApiConfigFailingScenarios, "gpAPiConfigFailingScenarios");

        // Default GpApi config (presumably using AppId and AppKey from base class/test context)
        GpApiConfig gpApiConfig = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(gpApiConfig);

        // MasterCard
        masterCard = new CreditCardData();
        masterCard.setNumber("5546259023665054");
        masterCard.setExpMonth(5);
        masterCard.setExpYear(2025);
        masterCard.setCvn("123");
        masterCard.setCardPresent(false);
        masterCard.setReaderPresent(false);

        // Visa
        visaCard = new CreditCardData();
        visaCard.setNumber("4263970000005262");
        visaCard.setExpMonth(expMonth);
        visaCard.setExpYear(expYear);
        visaCard.setCvn("123");
        visaCard.setCardPresent(true);
    }

    @Test
    public void creditSale_ShouldReturnsCapturedTransaction_WhenUsingLegacy5Config() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("LegacyPorticoConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnsCapturedTransaction_WhenUsingSecretApiKeyConfig() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("SecretApiKeyConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnCapturedTransaction_WithAllPorticoConfig() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("FullPorticoConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnCapturedTransaction_WhenUsingLegacy5ConfigWithAppId() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("LegacyPorticoAppIdConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnCapturedTransaction_WhenUsingSecretApiKeyConfigWithAppId() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("SecretApiKeyAppIdConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnCapturedTransaction_WhenUsingAllPorticoConfigWithAppId() throws ApiException {
        Transaction response = masterCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute("FullPorticoAppIdConfig");
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void creditSale_ShouldReturnCapturedTransaction_WhenUsingGpApiCredentials() throws ApiException {
        Transaction response = visaCard.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    // Verifies that a GatewayException is thrown when attempting a credit sale
    // with invalid credentials, and asserts the exception contains the expected
    // response code, message, and error details.
    @Test
    public void creditSale_ShouldThrowGatewayException_WhenCredentialsAreInvalid() {
        Exception exception = assertThrows(GatewayException.class, () -> {
            masterCard.charge(new BigDecimal("12"))
                    .withCurrency("USD")
                    .execute("gpAPiConfigFailingScenarios");
        });

        GatewayException ex = (GatewayException) exception;
        assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
        assertEquals("40004", ex.getResponseText());
        assertEquals("Status Code: 400 - Credentials not recognized to create access token", ex.getMessage());
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }
}