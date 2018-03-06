package com.global.api.tests;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.eCheck;
import com.global.api.services.ReportingService;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

public class BuilderValidationTests {
    CreditCardData card;
    
    public BuilderValidationTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com/Hps.Exchange.PosGateway/PosGatewayService.asmx");
        
        ServicesContainer.configure(config);
        
        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardHolderName("John Smith");
    }
    
    @Test(expected = BuilderException.class)
    public void creditAuthNoAmount() throws ApiException {
        card.authorize().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoCurrency() throws ApiException {
        card.authorize(new BigDecimal(14)).execute();
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoPaymentMethod() throws ApiException {
        card.authorize(new BigDecimal(14)).withCurrency("USD").withPaymentMethod(null).execute();
    }

    @Test(expected = BuilderException.class)
    public void creditSaleNoAmount() throws ApiException {
        card.charge().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditSaleNoCurrency() throws ApiException {
        card.charge(new BigDecimal(14)).execute();
    }

    @Test(expected = BuilderException.class)
    public void creditSaleNoPaymentMethod() throws ApiException {
        card.charge(new BigDecimal(14)).withCurrency("USD").withPaymentMethod(null).execute();
    }

    @Test(expected = BuilderException.class)
    public void creditOfflineNoAmount() throws ApiException {
        card.authorize().withOfflineAuthCode("12345").execute();
    }

    @Test(expected = BuilderException.class)
    public void creditOfflineNoCurrency() throws ApiException {
        card.authorize(new BigDecimal(14)).withOfflineAuthCode("12345").execute();
    }

    @Test(expected = BuilderException.class)
    public void creditOfflineNoAuthCode() throws ApiException {
        card.authorize(new BigDecimal(14)).withCurrency("USD").withOfflineAuthCode(null).execute();
    }

    @Test(expected = BuilderException.class)
    public void giftNoReplacementCard() throws ApiException {
        GiftCard gift = new GiftCard();
        gift.setAlias("1234567890");
        gift.replaceWith(null).execute();
    }

    @Test(expected = BuilderException.class)
    public void checkNoAddress() throws ApiException {
        new eCheck().charge(new BigDecimal(14)).withCurrency("USD").execute();
    }

    @Test(expected = BuilderException.class)
    public void benefitNoCurrency() throws ApiException {
        new EBTTrackData().benefitWithdrawal(new BigDecimal(10)).execute();
    }

    @Test(expected = BuilderException.class)
    public void benefitNoAmount() throws ApiException {
        new EBTTrackData().benefitWithdrawal().withCurrency("USD").execute();
    }

    @Test(expected = BuilderException.class)
    public void benefitNoPaymentMethod() throws ApiException {
        new EBTTrackData().benefitWithdrawal(new BigDecimal(10))
                .withCurrency("USD")
                .withPaymentMethod(null)
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void ReportTransactionDetailNoTransactionId() throws ApiException {
        ReportingService.transactionDetail(null).execute();
    }

    @Test(expected = BuilderException.class)
    public void ReportTransactionDetailWithDeviceId() throws ApiException {
        ReportingService.transactionDetail("123456789")
                .withDeviceId("123456")
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void ReportTransactionDetailWithStartDate() throws ApiException {
        ReportingService.transactionDetail("123456789")
                .withStartDate(new Date())
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void ReportTransactionDetailWithEndDate() throws ApiException {
        ReportingService.transactionDetail("123456789")
                .withEndDate(new Date())
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void ReportActivityWithTransactionId() throws ApiException {
        ReportingService.activity()
                .withTransactionId("1234567890")
                .execute();
    }
}
