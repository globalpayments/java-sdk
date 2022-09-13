package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoAutoSubTests {
    private CreditCardData card;
    private CreditTrackData track;

    public PorticoAutoSubTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn ("123");

        track = new CreditTrackData();
        track.setValue("<E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
        track.setEncryptionData(EncryptionData.version1());
    }

        @Test
    public void totalAmountTest() {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setClinicSubTotal(new BigDecimal("25"));
        autoSub.setVisionSubTotal(new BigDecimal("25"));
        autoSub.setDentalSubTotal(new BigDecimal("25"));
        autoSub.setPrescriptionSubTotal(new BigDecimal("25"));
        assertEquals(new BigDecimal("100"), autoSub.getTotalHelthcareAmount());
    }

        @Test
    public void dental() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setDentalSubTotal(new BigDecimal("150"));

        Transaction response = card.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        @Test
    public void vision() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setVisionSubTotal(new BigDecimal("150"));

        Transaction response = track.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        @Test
    public void clinicOrOther() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setClinicSubTotal(new BigDecimal("150"));

        Transaction response = card.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        @Test
    public void prescription() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setPrescriptionSubTotal(new BigDecimal("150"));

        Transaction response = track.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void allSubTotals() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setClinicSubTotal(new BigDecimal("25"));
        autoSub.setVisionSubTotal(new BigDecimal("25"));
        autoSub.setDentalSubTotal(new BigDecimal("25"));
        autoSub.setPrescriptionSubTotal(new BigDecimal("25"));

        card.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
    }

        @Test
    public void threeSubTotals() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setClinicSubTotal(new BigDecimal("25"));
        autoSub.setVisionSubTotal(new BigDecimal("25"));
        autoSub.setDentalSubTotal(new BigDecimal("25"));

        Transaction response = track.charge(new BigDecimal("215"))
            .withCurrency("USD")
            .withAllowDuplicates(true)
            .withAutoSubstantiation(autoSub)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        @Test
    public void twoSubTotals() throws ApiException {
        AutoSubstantiation autoSub = new AutoSubstantiation();
        autoSub.setMerchantVerificationValue("12345");
        autoSub.setRealTimeSubstantiation(false);
        autoSub.setClinicSubTotal(new BigDecimal("25"));
        autoSub.setVisionSubTotal(new BigDecimal("25"));

        Transaction response = card.charge(new BigDecimal("215"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAutoSubstantiation(autoSub)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}