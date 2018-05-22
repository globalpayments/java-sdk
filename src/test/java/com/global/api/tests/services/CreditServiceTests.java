package com.global.api.tests.services;

import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.CreditService;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CreditServiceTests {
    private CreditService service;
    private CreditCardData card;

    public CreditServiceTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        service = new CreditService(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
    }

    @Test
    public void creditServiceAuthCapture() throws ApiException {
        Transaction response = service.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditServiceSale() throws ApiException {
        Transaction response = service.charge(new BigDecimal("11.01"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditServiceEdit() throws ApiException {
        Transaction response = service.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction editResponse = service.edit(response.getTransactionId())
                .withAmount(new BigDecimal("14"))
                .withGratuity(new BigDecimal("2"))
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    @Test
    public void creditServiceCommercialEdit() throws ApiException {
        Transaction response = service.charge(new BigDecimal("13"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction editResponse = service.edit(response.getTransactionId())
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    @Test
    public void creditServiceRefundByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("14"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction refundResponse = service.refund(new BigDecimal("14"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }

    @Test
    public void creditServiceRefundByTransactionId() throws ApiException {
        Transaction response = service.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction refundResponse = service.refund(new BigDecimal("15"))
                .withCurrency("USD")
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
    }

    @Test
    public void creditServiceReverseByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("16"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("16"))
                .withPaymentMethod(card)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void creditServiceReverseByTransactionId() throws ApiException {
        Transaction response = service.charge(new BigDecimal("17"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("17"))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void creditServiceReverseByClientId() throws ApiException {
        Transaction response = service.charge(new BigDecimal("18"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withClientTransactionId("123456789")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("18"))
                .withClientTransactionId("123456789")
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void creditServiceVerify() throws ApiException {
        Transaction response = service.verify()
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditServiceVoid() throws ApiException {
        Transaction response = service.charge(new BigDecimal("19"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = service.voidTransaction(response.getTransactionId()).execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
}
