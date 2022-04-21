package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EcommerceChannel;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.tests.testdata.TestCards;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoEcommerceTests {
    private CreditCardData card;
    private String token;

    public PorticoEcommerceTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);

        card = TestCards.VisaManual();
        token = card.tokenize();
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
        ThreeDSecure ecom = new ThreeDSecure();
        ecom.setPaymentDataSource("ApplePay");
        ecom.setCavv("XXXXf98AAajXbDRg3HSUMAACAAA=");
        ecom.setEci("5");
        card.setThreeDSecure(ecom);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithWalletData_02() throws ApiException {
        ThreeDSecure ecom = new ThreeDSecure();
        ecom.setPaymentDataSource("ApplePay");
        ecom.setCavv("XXXXf98AAajXbDRg3HSUMAACAAA=");
        card.setThreeDSecure(ecom);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ecomWithSecure3D_03() throws ApiException {
        ThreeDSecure ecom = new ThreeDSecure();
        ecom.setCavv("XXXXf98AAajXbDRg3HSUMAACAAA=");
        ecom.setXid("0l35fwh1sys3ojzyxelu4ddhmnu5zfke5vst");
        ecom.setEci("5");
        ecom.setVersion(Secure3dVersion.ONE);
        card.setThreeDSecure(ecom);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore("Having the token invalidate known error for mobile transactions.")
    public void ecomWithWalletData_with_mobileType_04() throws ApiException {
        ThreeDSecure ecom = new ThreeDSecure();
        ecom.setPaymentDataSource("ApplePay");
        ecom.setCavv("XXXXf98AAajXbDRg3HSUMAACAAA=");
        card.setThreeDSecure(ecom);
        card.setMobileType(MobilePaymentMethodType.APPLEPAY);
        card.setToken(token);

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
