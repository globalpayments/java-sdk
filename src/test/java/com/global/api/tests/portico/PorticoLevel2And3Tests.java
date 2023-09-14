package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PorticoLevel2And3Tests {
    private CreditCardData card;
    private CreditTrackData track;
    private String clientTxnID;
    private CommercialData commercialData;

    public PorticoLevel2And3Tests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
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

        int randomID = new Random().nextInt(999999 - 10000)+10000;
        clientTxnID = Integer.toString(randomID);

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.Level_III) ;
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(DateTime.now());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setQuantity(new BigDecimal(1));

        // required for MC
        commercialLineItem.setUnitCost(new BigDecimal(0.01));
        commercialLineItem.setUnitOfMeasure("METER");
        commercialLineItem.setTotalAmount(new BigDecimal(10));

        DiscountDetails discountDetails = new DiscountDetails();
        discountDetails.setDiscountAmount(new BigDecimal(10));
        commercialLineItem.setDiscountDetails(discountDetails);

        commercialData.AddLineItems(commercialLineItem);
    }

    @Test
    public void level_ii_01_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(1));

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_ii_02_response_b() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("B", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_ii_03_visa_response_b() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withAddress(address)
                .withAllowDuplicates(true)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("B", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_ii_04_response_r() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("123.45"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("R", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_ii_05_MC_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(1));

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withAllowDuplicates(true)
                .withClientTransactionId(clientTxnID)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());


        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_ii_06_response_s() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withClientTransactionId(clientTxnID)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
        assertEquals(clientTxnID, cpcResponse.getClientTransactionId());
    }

    @Test
    public void level_ii_07_no_response() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.LevelII);
        commercialData.setPoNumber("9876543210");

        CreditCardData card = TestCards.AmexManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.10"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void level_iii__08_response_s_MC() throws ApiException {
        Address address = new Address("6860", "75024");

        int randomID = new Random().nextInt(999999 - 10000)+10000;
        clientTxnID = Integer.toString(randomID);

        CreditCardData card = TestCards.MasterCardManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
        assertEquals(clientTxnID, cpcResponse.getClientTransactionId());
    }

    @Test
    public void level_iii__09_response_s_Visa() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.Level_III) ;
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(DateTime.now());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setQuantity(new BigDecimal(1));

        DiscountDetails discountDetails = new DiscountDetails();
        discountDetails.setDiscountAmount(new BigDecimal(10));
        commercialLineItem.setDiscountDetails(discountDetails);

        commercialData.AddLineItems(commercialLineItem);

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withClientTransactionId(clientTxnID)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("S", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();

        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
        assertEquals(clientTxnID, cpcResponse.getClientTransactionId());
    }

    @Test
    public void level_iii_response() throws ApiException {
        Address address = new Address("6860", "75024");

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.Level_III) ;
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(DateTime.now());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setQuantity(new BigDecimal(1));

        DiscountDetails discountDetails = new DiscountDetails();
        discountDetails.setDiscountAmount(new BigDecimal(10));
        commercialLineItem.setDiscountDetails(discountDetails);

        commercialData.AddLineItems(commercialLineItem);

        CreditCardData card = TestCards.VisaManual();
        Transaction chargeResponse = card.charge(new BigDecimal("111.12"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .withClientTransactionId(clientTxnID)
                .execute();

        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());
        assertEquals("0", chargeResponse.getCommercialIndicator());

        Transaction cpcResponse = chargeResponse.edit()
                .withCommercialData(commercialData)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }
}
