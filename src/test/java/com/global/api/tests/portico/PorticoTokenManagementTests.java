package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.TransactionSummaryList;
import com.global.api.entities.enums.TimeZoneConversion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class PorticoTokenManagementTests {
    public PorticoTokenManagementTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
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