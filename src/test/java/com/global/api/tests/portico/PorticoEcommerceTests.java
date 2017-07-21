package com.global.api.tests.portico;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EcommerceChannel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class PorticoEcommerceTests {
    private CreditCardData card;

    public PorticoEcommerceTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        ServicesContainer.configure(config);

        card = TestCards.VisaManual();
    }

    @Test
    public void ecomWithMoto() throws ApiException {
        EcommerceInfo ecom = new EcommerceInfo();
        ecom.setChannel(EcommerceChannel.Moto);

        Transaction response = card.charge(new BigDecimal("9"))
            .withCurrency("USD")
            .withEcommerceInfo(ecom)
            .withAllowDuplicates(true)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithDirectMarketShipDate() throws ApiException {
        EcommerceInfo ecom = new EcommerceInfo();
        ecom.setShipDay(25);
        ecom.setShipMonth(12);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withEcommerceInfo(ecom)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithDirectMarketInvoiceNoShipDate() throws ApiException {
        EcommerceInfo ecom = new EcommerceInfo();
        ecom.setChannel(EcommerceChannel.Moto);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withEcommerceInfo(ecom)
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithDirectMarketDataInvoiceAndShipDate() throws ApiException {
        EcommerceInfo ecom = new EcommerceInfo();
        ecom.setShipDay(25);
        ecom.setShipMonth(12);

        Transaction response = card.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .withEcommerceInfo(ecom)
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithSecureEcommerce() throws ApiException {
        EcommerceInfo ecom = new EcommerceInfo();
        ecom.setPaymentDataSource("ApplePay");
        ecom.setCavv("XXXXf98AAajXbDRg3HSUMAACAAA=");
        ecom.setEci("5");

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withEcommerceInfo(ecom)
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
