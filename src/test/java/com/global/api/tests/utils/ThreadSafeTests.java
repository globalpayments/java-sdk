package com.global.api.tests.utils;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.tests.gpapi.BaseGpApiTest;
import lombok.var;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ThreadSafeTests extends BaseGpApiTest {

    @Test
    public void AddConfigThreadSafe() throws ApiException {
        var card = getCreditCardData();
        var configs = getGpApiConfigs();

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(config, config.getDynamicHeaders().get("configname"));
                    } catch (ConfigurationException e) {
                        e.printStackTrace();
                    }
                });

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        Transaction transaction =
                                card
                                        .charge(14)
                                        .withCurrency("USD")
                                        .withAllowDuplicates(true)
                                        .execute(config.getDynamicHeaders().get("configname"));

                        assertNotNull(transaction);
                        assertEquals("SUCCESS", transaction.getResponseCode());
                        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void AddRemoveConfigThreadSafe() {
        var card = getCreditCardData();
        var configs = getGpApiConfigs();

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(config, config.getDynamicHeaders().get("configname"));
                    } catch (ConfigurationException e) {
                        e.printStackTrace();
                    }
                });

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        Transaction transaction =
                                card
                                        .charge(14)
                                        .withCurrency("USD")
                                        .withAllowDuplicates(true)
                                        .execute(config.getDynamicHeaders().get("configname"));

                        assertNotNull(transaction);
                        assertEquals("SUCCESS", transaction.getResponseCode());
                        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(null, config.getDynamicHeaders().get("configname"));
                    } catch (ConfigurationException e) {
                        e.printStackTrace();
                    }
                });

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        var transaction =
                                card
                                        .charge(14)
                                        .withCurrency("USD")
                                        .withAllowDuplicates(true)
                                        .execute(config.getDynamicHeaders().get("configname"));
                    } catch (Exception ex) {
                        assertEquals("The specified configuration has not been configured for card processing.", ex.getMessage());
                    }
                });
    }

    @Test
    public void AddConfigChargeRemoveThreadSafe() {
        var card = getCreditCardData();

        getGpApiConfigs()
                .parallelStream()
                .forEach((config) -> {
                    try {
                        var configName = config.getDynamicHeaders().get("configname");
                        ServicesContainer.configureService(config, configName);

                        Transaction transaction =
                                card
                                        .charge(14)
                                        .withCurrency("USD")
                                        .withAllowDuplicates(true)
                                        .execute(config.getDynamicHeaders().get("configname"));

                        assertNotNull(transaction);
                        assertEquals("SUCCESS", transaction.getResponseCode());
                        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

                        ServicesContainer.configureService(null, configName);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    public void AddConfigException() {
        getGpApiConfigs()
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(config);
                    } catch (ConfigurationException configEx) {
                        assertEquals("Failed to add configuration: default.", configEx.getMessage());
                    }
                });
    }

    @Test
    public void AddRemoveConfigException() {
        var configs = getGpApiConfigs();

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(config);
                    } catch (ConfigurationException configEx) {
                        assertEquals("Failed to add configuration: default.", configEx.getMessage());
                    }
                });

        configs
                .parallelStream()
                .forEach((config) -> {
                    try {
                        ServicesContainer.configureService(null);
                    } catch (ConfigurationException configEx) {
                        assertEquals("Failed to remove configuration: default.", configEx.getMessage());
                    }
                });

    }

    private CreditCardData getCreditCardData() {
        var card = new CreditCardData();

        card.setNumber("4263970000005262");
        card.setExpMonth(5);
        card.setExpYear(2025);
        card.setCvn("852");
        card.setCardPresent(true);

        return card;
    }

    private ArrayList<GpApiConfig> getGpApiConfigs() {
        var configs = new ArrayList<GpApiConfig>();

        for (int i = 0; i < 100; i++) {
            GpApiConfig gpApiConfig = new GpApiConfig();
            gpApiConfig.setAppId("rkiYguPfTurmGcVhkDbIGKn2IJe2t09M");
            gpApiConfig.setAppKey("6gFzVGf40S7ZpjJs");
            gpApiConfig.setChannel(Channel.CardNotPresent);
            gpApiConfig.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
            gpApiConfig.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");
            gpApiConfig.setMerchantContactUrl("https://enp4qhvjseljg.x.pipedream.net/");
            gpApiConfig.setEnableLogging(true);

            HashMap<String, String> dynamicHeaders = new HashMap<>();
            dynamicHeaders.put("configname", "config" + i);

            gpApiConfig.setDynamicHeaders(dynamicHeaders);

            configs.add(gpApiConfig);
        }

        return configs;
    }

}