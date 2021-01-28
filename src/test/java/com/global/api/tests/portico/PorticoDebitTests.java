package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.ReversalReasonCode;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

public class PorticoDebitTests {
    private DebitTrackData track;

    public PorticoDebitTests() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        track = new DebitTrackData();
        track.setValue("&lt;E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        track.setPinBlock("32539F50C245A6A93D123412324000AA");
        track.setEncryptionData(EncryptionData.version1());
    }

    @Test
    public void debitSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal("14.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test @Ignore
    public void debitAddValue() throws ApiException {
        Transaction response = track.addValue(new BigDecimal("15.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitRefund() throws ApiException {
        Transaction response = track.refund(new BigDecimal("16.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitReverse() throws ApiException {
        Transaction response = track.charge(new BigDecimal("17.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        response = track.reverse(new BigDecimal("17.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void debitRefundFromTransactionId() throws ApiException {
        Transaction.fromId("1234567890", PaymentMethodType.Debit).refund().execute();
    }

    @Test
    public void debitReverseFromTransactionId() throws ApiException {
        Transaction response = track.charge(new BigDecimal("12.21"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal("12.21"))
                .withReversalReasonCode(ReversalReasonCode.Timeout)
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void debitInteracPosNumber() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withPosSequenceNumber("1")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void debitInteracWithCardHolderLanguage() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withPosSequenceNumber("1")
                .withCardHolderLanguage("en-US")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracAccountTypeChecking() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAccountType(AccountType.Checking)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracAccountTypeSavings() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAccountType(AccountType.Savings)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracMessageAuthenticationCode() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withMessageAuthenticationCode("AuthCode")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracChipConditionFailed() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracChipConditionFailedTwice() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitSaleWithNewCryptoURL() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

        ServicesContainer.configureService(config);
        Transaction response = track.charge(new BigDecimal("14.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
