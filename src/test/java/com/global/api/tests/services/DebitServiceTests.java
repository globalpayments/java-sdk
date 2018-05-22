package com.global.api.tests.services;

import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.DebitService;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DebitServiceTests {
    private DebitService service;
    private DebitTrackData card;

    public DebitServiceTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        service = new DebitService(config);

        card = new DebitTrackData();
        card.setValue("&lt;E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        card.setPinBlock("32539F50C245A6A93D123412324000AA");
        card.setEncryptionData(EncryptionData.version1());
    }

    @Test
    public void debitServiceSale() throws ApiException {
        Transaction response = service.charge(new BigDecimal("14"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitServiceRefundByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("14"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
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
    public void debitServiceReverseByCard() throws ApiException {
        Transaction response = service.charge(new BigDecimal("16"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = service.reverse(new BigDecimal("16"))
                .withPaymentMethod(card)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void debitServiceReverseByTransactionId() throws ApiException {
        Transaction response = service.charge(new BigDecimal("17"))
                .withCurrency("USD")
                .withPaymentMethod(card)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // TODO: Figure out how to handle multiple payment methods... thanks plano...
        Transaction reverseResponse = service.reverse(new BigDecimal("17"))
                .withCurrency("USD")
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }
}
