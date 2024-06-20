package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TimeZoneConversion;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.SurchargeLookup;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import com.global.api.services.ReportingService;
import com.global.api.services.SurchargeEligibilityService;
import com.global.api.utils.DateUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class PorticoReportingTests {
    public PorticoReportingTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);
        ServicesContainer.configureService(config);
    }

    @Test
    public void ReportActivity() throws ApiException {
        List<TransactionSummary> summary = ReportingService.activity()
                .withStartDate(DateUtils.addDays(new Date(), -7))
                .withEndDate(DateUtils.addDays(new Date(), -1))
                .execute();
        assertNotNull(summary);
    }

    @Test
    public void ReportTransactionDetail() throws ApiException {
        TransactionSummaryList summary = ReportingService.findTransactions()
                .withStartDate(DateUtils.addDays(new Date(), -7))
                .withEndDate(DateUtils.addDays(new Date(), -1))
                .execute();

        if (summary.size() > 0) {
            TransactionSummary response = ReportingService.transactionDetail(summary.get(0).getTransactionId()).execute();
            assertNotNull(response);
            assertEquals("00", response.getGatewayResponseCode());
        }
    }

    @Test
    public void ReportActivityWithNewCryptoURL() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w");
        config.setServiceUrl("https://cert.api2-c.heartlandportico.com");

        ServicesContainer.configureService(config);
        List<TransactionSummary> summary = ReportingService.activity()
                .withStartDate(DateUtils.addDays(new Date(), -7))
                .withEndDate(DateUtils.addDays(new Date(), -1))
                .execute();
        assertNotNull(summary);
    }

    @Test
    public void ReportTransactionDetailWithTransactionId() throws ApiException {
        TransactionSummary response = ReportingService.transactionDetail("1038021900")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void CheckSurchargeEligibility() throws ApiException{
        CreditCardData card = new CreditCardData();
        card.setNumber("374245001751006");
        card.setExpMonth(12);
        card.setExpYear(2024);
        card.setCardPresent(true);

        EncryptionData enc = new EncryptionData();
        enc.setVersion("05");
        enc.setTrackNumber("2");
        enc.setKsn("//89P4EAEkAACg==");
        CreditTrackData track = new CreditTrackData();
        track.setValue("Cm2HEnFWHPQnW+96DaaAHRz/+LMKe2lo");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(enc);
        SurchargeLookup manual = SurchargeEligibilityService.eligibilityLookup().withPaymentMethod(card)
                .execute();
        SurchargeLookup withTrack = SurchargeEligibilityService.eligibilityLookup().withPaymentMethod(track)
                .execute();
        assertNotNull(manual);
        assertNotNull(withTrack);
    }

    @Test
    public void ReportFindTransactionWithTransactionId() throws ApiException {
        TransactionSummary response = ReportingService.findTransactions("1038021900")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionNoCriteria() throws ApiException {
        TransactionSummaryList response = ReportingService.findTransactions()
                .execute();
        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionWithSAFIndicatorCriteria() throws ApiException {
        TransactionSummaryList response = ReportingService.findTransactions()
                .where(SearchCriteria.SafIndicator,"N")
                .execute();
        assertNotNull(response);
    }

    @Test
    public void ReportFindTransactionWithCriteria() throws ApiException {
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setCardHolderName("John Doe");
        creditCardData.setNumber("4242424242429876");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("123");

        creditCardData.charge(new BigDecimal(15))
            .withCurrency("usd")
            .withInvoiceNumber("11115")
            .withAllowDuplicates(true)
            .execute();

        creditCardData.charge(new BigDecimal(10))
            .withCurrency("usd")
            .withInvoiceNumber("776655")
            .withAllowDuplicates(true)
            .execute();

        creditCardData.charge(new BigDecimal(10))
            .withCurrency("usd")
            .withInvoiceNumber("776655")
            .withAllowDuplicates(true)
            .execute();

        TransactionSummaryList summary = ReportingService.findTransactions()
            .where(SearchCriteria.CardNumberLastFour, "9876")
            .execute();

        assertNotNull(summary);

        if( (summary.size() % 3) != 0 ) {
            fail(String.format("Test should have produced 3 results. %s transactions were found.", summary.size()));
        }

        TransactionSummaryList summary2 = ReportingService.findTransactions()
            .where(SearchCriteria.SettlementAmount, new BigDecimal(10))
            .and(SearchCriteria.InvoiceNumber, "776655")
            .execute();

        assertNotNull(summary2);

        if( (summary2.size() % 2) != 0 ) {
            fail(String.format("Test should have produced 2 results. %s transactions were found.", summary2.size()));
        }

        TransactionSummaryList summary3 = ReportingService.findTransactions()
            .where(SearchCriteria.InvoiceNumber, "11115")
                .and(SearchCriteria.SafIndicator,"N")
            .execute();

        assertNotNull(summary3);

        if( (summary3.size() % 1) != 0 ) {
            fail(String.format("Test should have produced 1 result. %s transactions were found.", summary3.size()));
        }
    }

    @Test
    public void ReportFindTransactionWithCardHolderName() throws ApiException {
        CreditCardData creditCardData = new CreditCardData();
        creditCardData.setCardHolderName("John Doe");
        creditCardData.setNumber("4242424242424242");
        creditCardData.setExpMonth(12);
        creditCardData.setExpYear(2025);
        creditCardData.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setCity("Jeffersonville");
        address.setState("IN");
        address.setPostalCode("47130");

        Transaction trResponse = creditCardData.charge(new BigDecimal(10))
                .withCurrency("usd")
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .execute();

        assertNotNull(trResponse);
        assertEquals("00", trResponse.getResponseCode());

        TransactionSummary response = ReportingService.findTransactions(trResponse.getTransactionId())
                .withTimeZoneConversion(TimeZoneConversion.Merchant)
                .execute();
        assertNotNull(response);
        assertNotNull(response.getCardHolderFirstName());
        assertNotNull(response.getCardHolderLastName());
        assertNotNull(response.getResponseDate());
        assertNotEquals("", response.getCardHolderFirstName());
        assertNotEquals("", response.getCardHolderLastName());
        assertNotEquals("", response.getResponseDate());
    }
}