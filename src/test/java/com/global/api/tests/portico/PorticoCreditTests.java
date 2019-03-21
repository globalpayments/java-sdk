package com.global.api.tests.portico;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.ReportingService;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoCreditTests {
    private CreditCardData card;
    private CreditTrackData track;

    public PorticoCreditTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setDeveloperId("002914");
        config.setVersionNumber("3026");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        track = new CreditTrackData();
        track.setValue("<E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
        track.setEncryptionData(EncryptionData.version1());
    }

    @Test
    public void creditAuthorization() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2)).execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditAuthWithConvenienceAmt() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withConvenienceAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getConvenienceAmount());
    }

    @Test
    public void creditAuthWithShippingAmt() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withShippingAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getShippingAmount());
    }

    @Test
    public void creditSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithConvenienceAmt() throws ApiException {
        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withConvenienceAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getConvenienceAmount());
    }

    @Test
    public void creditSaleWithShippingAmt() throws ApiException {
        Transaction response = card.charge(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withShippingAmt(new BigDecimal(2))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TransactionSummary report = ReportingService.transactionDetail(response.getTransactionId()).execute();
        assertNotNull(report);
        assertEquals(new BigDecimal("2.00"), report.getShippingAmount());
    }

    @Test
    public void creditOfflineAuth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(16))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditOfflineSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(17))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditRefund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(16))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditReverse() throws ApiException {
        Transaction response = card.charge(new BigDecimal(18))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        response = card.reverse(new BigDecimal(18))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVerify() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeAuthorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(16)).withGratuity(new BigDecimal(2)).execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void creditSwipeSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeOfflineAuth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(16))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeOfflineSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(17))
                .withCurrency("USD")
                .withOfflineAuthCode("12345")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test @Ignore
    public void creditSwipeAddValue() throws ApiException {
        Transaction response = track.addValue(new BigDecimal(16))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeBalanceInquiry() throws ApiException {
        Transaction response = track.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeRefund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(16))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSwipeReverse() throws ApiException {
        Transaction response = track.charge(new BigDecimal(19))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = track.reverse(new BigDecimal(19))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }

    @Test
    public void creditSwipeVerify() throws ApiException {
        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVoidFromTransactionId() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = Transaction.fromId(response.getTransactionId())
                .voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void creditTestWithNewCryptoURL() throws ApiException {
         GatewayConfig config = new GatewayConfig();
         config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
         config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

         ServicesContainer.configureService(config);

         card = new CreditCardData();
         card.setNumber("4111111111111111");
         card.setExpMonth(12);
         card.setExpYear(2025);
         card.setCvn("123");

         Transaction response = card.authorize(new BigDecimal(14))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
         assertNotNull(response);
         assertEquals("00", response.getResponseCode());

    }
}
