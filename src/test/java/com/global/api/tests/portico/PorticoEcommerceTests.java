package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.EcommerceChannel;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.Secure3dVersion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.services.ReportingService;
import com.global.api.tests.testdata.TestCards;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class PorticoEcommerceTests {
    private CreditCardData card;
    private String token;

    public PorticoEcommerceTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTnHBQBkVnIApt5_DIG_OTix0zXDR-7UQMAx6focuA");
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
    public void testCardHolderEmail() throws ApiException {
        String customerEmail = "john.doe@test.com";
        Customer customer = new Customer();
        customer.setEmail(customerEmail);
        Transaction response = card.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .withCustomerData(customer)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary txnSummary = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(txnSummary);
        assertEquals(customerEmail, txnSummary.getEmail() );
    }
    @Test
    public void testCardHolderEmailIfCustomerDataIsNull() throws ApiException {
        Customer customerData = null;

        Transaction response = card.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .withCustomerData(customerData)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary txnSummary = ReportingService.transactionDetail(response.getTransactionId()).execute();

        assertNotNull(txnSummary);
        assertEquals("", txnSummary.getEmail() );
    }

    @Test
    @Ignore("Having the token invalidate known error for mobile transactions.")
    public void ecomWithWalletData_with_mobileType_04() throws ApiException {
        Address addy = new Address();
        addy.setPostalCode("56789");

        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);
        card.setPaymentDataSourceType(PaymentDataSourceType.GOOGLEPAYWEB);
        card.setToken("{\"signature\":\"MEYCIQCIIwGEk/CH0wEq5CwljA0F2+IGPuE201FgF3uwRvmqFwIhAIRScN6Amim13SvoPO5DudEKQ/FpCy+1PhVyTKLDlKCC\",\"protocolVersion\":\"ECv1\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"6QT/y8Qep7unsjxtv2QmoE0G+CiONgHA+SGAlizsjWbokS6mlTM63Y3XJii9544Nlj3dqWK86rsXu6bscnNZFmMdjlPRtCJSetvzP3uuuvDtlp/2bOscONy30dfwld+/XoxwwGzTepx9VeiG5j5jvuxuCGSI8cpMoT/ILLYx0rrxn1C/UBb8VU2kgbfPPsrVO2ODMtRLzpoQuOKYiKOi9DPebqkw/By4laaSD58clgRgbZ9yfsQUhiIWzEmJD4+l7J1YdZ+/6zqhsNLrV4vUmzpyx4C5FS4ZvSsXpTsCBryKotcMIBfWlsuo9u/S4kNori7fUchVj+aw6Yeik7LqG5ARAfYNFHaSkq4J9/2IbVVgeJiRAIpZ1s3qB/cm3foZHOzOtk0E6xKswj8Su2wHtx75PzSb9J01o4JGNmng7EIxejOxve6vot6HfMx2kAS0TXMFiA2F/Q\\\\u003d\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BJApLY3ZMqKxi+eVDkh59mugSa74fTLYNHxQG2wSks29bzwRRktCzyUQyyrIJM2KLonxH1eIFmDfz6ID9dK2PGs\\\\u003d\\\",\\\"tag\\\":\\\"YQnZEkEBPGcyR1p1NkLzvoA+6druD8X6ghrqGwQlyUA\\\\u003d\\\"}\"}");

        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .withAddress(addy)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    @Ignore("Having the token invalidate known error for mobile transactions.")
    public void ecomAuthWithWalletData_with_mobileType_05() throws ApiException {
        card.setMobileType(MobilePaymentMethodType.GOOGLEPAY);
        card.setPaymentDataSourceType(PaymentDataSourceType.GOOGLEPAYWEB);
        card.setToken("{\"signature\":\"MEUCIQD07r8rLOPzJllzc59KpX4rJLO1Jc/eJpSV2BYDW4DA/QIgCYxYcQDeTDyq5jsXjuTSFEyaec91/gzEhjh/DAVvJTk\\u003d\",\"protocolVersion\":\"ECv1\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"zTAV12J1ZmESQMPGR3oKxpkByfzACJ8S2BPv+qHdAFkwDQxRm/pHzphWYER456CcGCWSwqzx+3f7UP/X88X/7uA3pmU/5SSJ+Lmp4ooBZ96ru28m5CljgKfqGP9V8tWORWa8w4Qlv52OQjDVBQvvNnZehp/W4Fq6hRe6hPNCZXHRrUlGeTeMwgPxN4Ght1npMt8eDr5/l5D+tgmtaRjNE1Z589gMKue0GsZ+99fp0L5J06Ya4ffQdfvin9Lv202Mhf/ars2HhIv69G5nwmi/K0bxiEru6dmNIXWoGggQfNR6c44w6RhyypndTFAf8//JSHOn+qWA7qtqNbmpwT3++j9WPHv5aARZcBDjf+aqYKftbR19dXEUkcGD2olPI6tHwmov4FBP3V7vGy+oqp9xI0KkMgx87rlkBDbn03iIvnwPupR38evcw/OTU7cLtp4aUUf4iNpd0A\\\\u003d\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BES91gT+rXPmELU4EsPxz+PyZF6f2+A7sUcKt9/SnSPJ44j/YBrxJaIVgJbqsUyKyDoxWCJ2IkI2fOO5VgMCiI4\\\\u003d\\\",\\\"tag\\\":\\\"9PPp3uJgWfBjP6oJOqr8KzGFX/z7eUNj9MDNN+E55mc\\\\u003d\\\"}\"}");

        Transaction response = card.authorize(new BigDecimal("10"))
                .withCurrency("USD")
                .withInvoiceNumber("1234567890")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void UniqueTokenRequest() throws ApiException {
        card = TestCards.VisaManual();

        Transaction response1 = card.verify()
                .withRequestMultiUseToken(true, false)
                .execute();

        assertNotNull(response1);
        assertNotNull(response1.getToken());

        Transaction response2 = card.verify()
                .withRequestMultiUseToken(true,false)
                .execute();

        assertNotNull(response2);
        assertNotNull(response2.getToken());
        assertEquals(response1.getToken(), response2.getToken());

        Transaction response3 = card.verify()
                .withRequestMultiUseToken(true, true)
                .execute();

        assertNotNull(response3);
        assertNotNull(response2.getToken());
        assertNotEquals(response1.getToken(),response3.getToken());
    }
}