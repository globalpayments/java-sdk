package com.global.api.tests.services;

import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.EbtService;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EbtServiceTests {
    private EbtService service;
    private EBTTrackData card;

    public EbtServiceTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        service = new EbtService(config);

        card = TestCards.asEBT(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");
    }

    @Test
    public void EbtServiceBalanceInquiry() throws ApiException {
        Transaction response = service.balanceInquiry()
                .withPaymentMethod(card)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void EbtServiceBenefitsWithdrawal() throws ApiException {
        Transaction response = service.benefitWithdrawal(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void EbtServiceSale() throws ApiException {
        Transaction response = service.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void EbtServiceRefundByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction refundResponse = service.refund(new BigDecimal("12"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }
}
