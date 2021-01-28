package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

public class PorticoEbtTests {
    private EBTCardData card;
    private EBTTrackData track;
    
    public PorticoEbtTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);
        
        card = new EBTCardData();
        card.setNumber("4012002000060016");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setPinBlock("32539F50C245A6A93D123412324000AA");

        track = new EBTTrackData();
        track.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        track.setPinBlock("32539F50C245A6A93D123412324000AA");
        track.setEncryptionData(EncryptionData.version1());
    }

    @Test
    public void ebtBalanceInquiry() throws ApiException {
        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void ebtSaleWithCardHolderLanguage() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withCardHolderLanguage("en-US")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtRefund() throws ApiException {
        Transaction response = card.refund(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtTrackBalanceInquiry() throws ApiException {
        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtTrackSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtTrackRefund() throws ApiException {
        Transaction response = card.refund(new BigDecimal("11"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void ebtRefundFromTransactionId() throws ApiException {
        Transaction.fromId("1234567890", PaymentMethodType.EBT).refund().execute();
    }
}
