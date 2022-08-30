package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.EcommerceInfo;
import com.global.api.entities.PaymentDataSourceType;
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
        ecom.setPaymentDataSource(PaymentDataSourceType.APPLEPAY);
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
        ecom.setPaymentDataSource(PaymentDataSourceType.APPLEPAY);
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
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);
        card.setPaymentDataSourceType(PaymentDataSourceType.GOOGLEPAYWEB);
        card.setToken("{\"signature\":\"MEYCIQDn1WUTJSbe0HTenhQBanye9MNlEEbJ9nvk2YDE11JO1wIhAOkA99r3sMpuHsQqdR1C8u9R7C7dm9w7wNniXtYr01gv\",\"protocolVersion\":\"ECv1\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"BxaNW7Rxei+P0lBvb2jvE8+HQ04/uAFrHIXynZqsM7p6rxFDmgt7JxE8XnTnGacTyOXAITFlHnqD5eZJ6dQGMn/DHdhjmi/El25J2rpOzZiJPQk394YQLY2xjUm1xIDR3GB1ATfBIRKoqtf2iXiYQ/u50XINut0ivK/u+qc3lbDAC3IrDUq5DED7uPcPhijF2snKL5sROatKiecfTQRzWMJioTZXDaYfQseoWhhFVvO/UpEcK5CZh5b3CQT89yzDPPdwa1XSH+8DYK6UxvBoelaLYIxpLUNBFcUurLukBM24VlzG5Rs8os8hOXXLixcIcDuiFH4MS7wMIAW4DtKvZF7E78xvh2IvlxckoJ6uZsVuyGBgXgjIgbn95lqeMZsR398YcY/lDl5N/HCpxDJbvSQfd7YNf/hEK/NAa15AAScQ6sorFYcFF1W1iU3+gBR+fuIODT/1VQ\\\\u003d\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BLTKhwsuoS/Izu5fYd08D+HAd2TAc+FTmEpa7L4wo45p3hQbZ3agZ9J60v8agMsXiDIXpbN1VlBpibKezSFxfoU\\\\u003d\\\",\\\"tag\\\":\\\"hbkBnamtgcDYeDrJvY3IKAzOU4E4aFS2cJUK4f5VVxM\\\\u003d\\\"}\"}");

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
