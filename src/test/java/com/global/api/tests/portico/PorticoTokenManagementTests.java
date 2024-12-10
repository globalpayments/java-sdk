package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;

import com.global.api.serviceConfigs.PorticoConfig;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PorticoTokenManagementTests {
    public PorticoTokenManagementTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);
    }

    @Test
    public void updateToken() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(DateTime.now().getYear() + 1);
        card.setCvn("123");

        Transaction response = card.verify()
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response.getToken());

        CreditCardData cardAsToken = new CreditCardData();
        cardAsToken.setToken(response.getToken());
        cardAsToken.setExpYear(2026);
        cardAsToken.setExpMonth(1);

        assertTrue(cardAsToken.updateTokenExpiry());
    }

    @Test
    public void deleteToken() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("5454545454545454");
        card.setExpMonth(12);
        card.setExpYear(DateTime.now().getYear() + 1);
        card.setCvn("123");

        Transaction response = card.verify()
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response.getToken());

        CreditCardData cardAsToken = new CreditCardData();
        cardAsToken.setToken(response.getToken());

        assertTrue(cardAsToken.deleteToken());
    }
}
