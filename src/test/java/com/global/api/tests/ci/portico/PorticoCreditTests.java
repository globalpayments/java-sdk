package com.global.api.tests.ci.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.services.ReportingService;
import com.global.api.tests.utils.citesting.CiTestingHarness;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PorticoCreditTests {
    private static final CiTestingHarness ciTestingHarness = new CiTestingHarness(
            "https://cert.api2.heartlandportico.com",
            CiTestingHarness.CacheMode.Locked,
            "PorticoCreditTests");

    private final CreditTrackData track;
    private final CommercialData commercialData;
    private CreditCardData card;

    public PorticoCreditTests() throws ApiException {
        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        track = new CreditTrackData();
        track.setValue("<E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
        track.setEncryptionData(EncryptionData.version1());

        commercialData = new CommercialData(TaxType.SalesTax, TransactionModifier.Level_III);
        commercialData.setPoNumber("9876543210");
        commercialData.setTaxAmount(new BigDecimal(10));
        commercialData.setDestinationPostalCode("85212");
        commercialData.setDestinationCountryCode("USA");
        commercialData.setOriginPostalCode("22193");
        commercialData.setSummaryCommodityCode("SSC");
        commercialData.setCustomerReferenceId("UVATREF162");
        commercialData.setOrderDate(ciTestingHarness.getCurrentTime());
        commercialData.setFreightAmount(new BigDecimal(10));
        commercialData.setDutyAmount(new BigDecimal(10));

        AdditionalTaxDetails ad = new AdditionalTaxDetails();
        ad.setTaxAmount(new BigDecimal(10));
        ad.setTaxRate(new BigDecimal(10));

        commercialData.setAdditionalTaxDetails(ad);
        CommercialLineItem commercialLineItem = new CommercialLineItem();

        commercialLineItem.setDescription("PRODUCT 1 NOTES");
        commercialLineItem.setProductCode("PRDCD1");
        commercialLineItem.setUnitCost(new BigDecimal("0.01"));
        commercialLineItem.setQuantity(new BigDecimal(1));
        commercialLineItem.setUnitOfMeasure("METER");
        commercialLineItem.setTotalAmount(new BigDecimal(10));

        DiscountDetails discountDetails = new DiscountDetails();
        discountDetails.setDiscountAmount(new BigDecimal(1));
        commercialLineItem.setDiscountDetails(discountDetails);

        commercialData.AddLineItems(commercialLineItem);
    }

    private void configurePorticoService() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl(ciTestingHarness.getTestingUrl());
        config.setDeveloperId("002914");
        config.setVersionNumber("3026");
        config.setEnableLogging(true);

        ciTestingHarness.attach(config);
    }



    @Test
    public void creditSale() throws ApiException {
        ciTestingHarness.setFunction("Portico|Credit Transactions|CreditSale");
        configurePorticoService();
        String clientTxnID = ciTestingHarness.generateRandomId("creditSale");
        Transaction response = card.charge(new BigDecimal(15.5))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withUniqueDeviceId("5678")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(clientTxnID, response.getClientTransactionId());
    }


    @Test
    public void creditTxnEdit() throws ApiException {
        ciTestingHarness.setFunction("Portico|Credit Transactions|CreditTxnEdit - aka Gratuity");
        configurePorticoService();
        String clientTxnID = ciTestingHarness.generateRandomId("creditTxnEdit_charge");
        Transaction chargeResponse = card.charge(new BigDecimal(15))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("00", chargeResponse.getResponseCode());

        String editClientTxnID = ciTestingHarness.generateRandomId("creditTxnEdit_edit");
        Transaction editResponse = chargeResponse.edit(new BigDecimal(17))
                .withCurrency("USD")
                .withGratuity(new BigDecimal(2))
                .withClientTransactionId(editClientTxnID)
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
        assertEquals(editClientTxnID, editResponse.getClientTransactionId());
    }

    @Test
    public void creditAdditionalAuth() throws ApiException {
        ciTestingHarness.setFunction("Portico|Credit Transactions|CreditAdditionalAuth");
        configurePorticoService();
        String clientTxnID = ciTestingHarness.generateRandomId("creditAdditionalAuth_auth");
        Transaction authResponse = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withClientTransactionId(clientTxnID)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());
        assertEquals(clientTxnID, authResponse.getClientTransactionId());

        String additionalAuthClientTxnID = ciTestingHarness.generateRandomId("creditAdditionalAuth_additional");
        Transaction additionalAuthResponse = Transaction.fromId(authResponse.getTransactionId())
                .additionalAuth(new BigDecimal(10))
                .withCurrency("USD")
                .withClientTransactionId(additionalAuthClientTxnID)
                .execute();
        assertNotNull(additionalAuthResponse);
        assertEquals("00", additionalAuthResponse.getResponseCode());
        assertEquals(additionalAuthClientTxnID, additionalAuthResponse.getClientTransactionId());
    }
}

