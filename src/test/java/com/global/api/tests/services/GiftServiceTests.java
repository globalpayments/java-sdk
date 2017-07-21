package com.global.api.tests.services;

import com.global.api.ServicesConfig;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.services.GiftService;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GiftServiceTests {
    private GiftService service;
    private GiftCard card;
    private GiftCard replacement;

    public GiftServiceTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        service = new GiftService(config);

        card = new GiftCard();
        card.setNumber("5022440000000000007");
        
        replacement = new GiftCard();
        replacement.setTrackData("%B5022440000000000098^^391200081613?;5022440000000000098=391200081613?");
    }

    @Test
    public void GiftServiceActivate() throws ApiException {
        Transaction response = service.activate(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceAddValue() throws ApiException {
        Transaction response = service.addValue(new BigDecimal("11"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceAddAlias() throws ApiException {
        Transaction response = service.addAlias("2145550199")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceBalanceInquiry() throws ApiException {
        Transaction response = service.balanceInquiry()
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceCharge() throws ApiException {
        Transaction response = service.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceDeactivate() throws ApiException {
        Transaction response = service.deactivate()
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceRemoveAlias() throws ApiException {
        Transaction response = service.removeAlias("2145550199")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceReplaceWith() throws ApiException {
        Transaction response = service.replaceWith(replacement)
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceReverseByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("10"))
                .withPaymentMethod(card)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void GiftServiceReverseByTransactionId() throws ApiException {
        Transaction response = service.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("10"))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void GiftServiceRewards() throws ApiException {
        Transaction response = service.rewards(new BigDecimal("15"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void GiftServiceVoid() throws ApiException {
        Transaction response = service.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = service.voidTransaction(response.getTransactionId()).execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
}
