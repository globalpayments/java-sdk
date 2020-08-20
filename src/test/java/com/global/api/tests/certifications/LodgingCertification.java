package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.LodgingData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.Credit;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LodgingCertification {
    public LodgingCertification() throws ApiException {
        GatewayConfig config = new GatewayConfig();
        config.setSiteId(20768);
        config.setLicenseId(20767);
        config.setDeviceId(1414594);
        config.setUsername("777700003011");
        config.setPassword("$Test1234");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);
        config.setDeveloperId("002914");
        config.setVersionNumber("3057");

        ServicesContainer.configureService(config);
    }

    @Test
    public void lodging_000_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            TestCase.assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (!exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                Assert.fail(exc.getMessage());
        }
    }

    /*
    Check In / Check Out (Single Stay) - SALE : SWIPED
     */

    @Test
    public void lodging_001_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(99);

        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_002a_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.charge(new BigDecimal("11"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_002b_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.MasterCard24Swipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.charge(new BigDecimal("11.50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_003_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.charge(new BigDecimal("12"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_004_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.charge(new BigDecimal("13"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_005_SaleSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.JcbSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.charge(new BigDecimal("14"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    Check In / Check Out (Single Stay)  - SALE : KEYED, CARD PRESENT
     */

    @Test
    public void lodging_006_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("15"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_007a_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("16"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_007b_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardSeries2Manual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("16.50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_008_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("17"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_009_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("18"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_010_SaleKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.JcbManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("19"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    Check In / Check Out (Single Stay)  - SALE : KEYED, CARD NOT PRESENT
     */

    @Test
    public void lodging_011_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("20"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_012a_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("21"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_012b_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardSeries2Manual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("21.50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_013_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("22"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_014_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("23"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_015_SaleKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.JcbManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.charge(new BigDecimal("24"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /* Contactless */

    /*
    Check In / Check Out - AUTHORIZATIONS : SWIPED
     */

    @Test
    public void lodging_016_AuthSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.authorize(new BigDecimal("25"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_017_AuthSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.authorize(new BigDecimal("26"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_018_AuthSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.authorize(new BigDecimal("27"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_019_AuthSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.authorize(new BigDecimal("28"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_020_AuthSwiped_SingleStay() throws ApiException {
        CreditTrackData track = TestCards.JcbSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = track.authorize(new BigDecimal("29"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    Check In / Check Out - AUTHORIZATIONS : KEYED, CARD PRESENT
     */

    @Test
    public void lodging_021_AuthKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("30"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_022_AuthKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("31"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_023_AuthKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("32"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_024_AuthKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("33"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_025_AuthKeyed_CardPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.JcbManual(true, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("34"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    Check In / Check Out - AUTHORIZATIONS : KEYED, CARD NOT PRESENT
     */

    @Test
    public void lodging_026_AuthKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("35"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_027_AuthKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("36"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_028_AuthKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("37"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_029_AuthKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("38"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_030_AuthKeyed_CardNotPresent_SingleStay() throws ApiException {
        CreditCardData card = TestCards.JcbManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);

        Transaction response = card.authorize(new BigDecimal("39"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    Advanced Deposit - SALES
     */

    @Test
    public void lodging_031_SaleKeyed_AdvancedDeposit() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setAdvancedDepositType(AdvancedDepositType.CardDeposit);

        Transaction response = card.charge(new BigDecimal("41"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_032_SaleKeyed_AdvancedDeposit() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(2);
        lodgingData.setAdvancedDepositType(AdvancedDepositType.CardDeposit);

        Transaction response = card.charge(new BigDecimal("80"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    No Show - SALES
     */

    @Test
    public void lodging_033_SaleKeyed_NoShow() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setNoShow(true);

        Transaction response = card.charge(new BigDecimal("42"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_034_SaleKeyed_NoShow() throws ApiException {
        CreditCardData card = TestCards.AmexManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setAdvancedDepositType(AdvancedDepositType.AssuredReservation);
        lodgingData.setNoShow(true);

        Transaction response = card.charge(new BigDecimal("43"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    Visa Prestigious Property - SALES
     */

    @Test
    public void lodging_035_SaleSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_500);

        Transaction response = track.charge(new BigDecimal("44"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_036_SaleSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_1000);

        Transaction response = track.charge(new BigDecimal("45"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_037_SaleSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_1500);

        Transaction response = track.charge(new BigDecimal("46"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    Visa Prestigious Property - AUTHORIZATIONS
     */

    @Test
    public void lodging_038_AuthSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_500);

        Transaction response = track.authorize(new BigDecimal("44"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_039_AuthSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_1000);

        Transaction response = track.authorize(new BigDecimal("45"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_040_AuthSwiped_PrestigiousProperty() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPrestigiousPropertyLimit(PrestigiousPropertyLimit.Limit_1500);

        Transaction response = track.authorize(new BigDecimal("46"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    MasterCard Preferred Customer - SALES
     */

    @Test
    public void lodging_041_SaleSwiped_PreferredCustomer() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPreferredCustomer(true);

        Transaction response = track.charge(new BigDecimal("47"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_042_SaleKeyed_PreferredCustomer() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPreferredCustomer(true);

        Transaction response = card.charge(new BigDecimal("48"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    MasterCard Preferred Customer - AUTHORIZATIONS
     */

    @Test
    public void lodging_043_AuthSwiped_PreferredCustomer() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPreferredCustomer(true);

        Transaction response = track.authorize(new BigDecimal("47"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_044_AuthKeyed_PreferredCustomer() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        LodgingData lodgingData = new LodgingData();
        lodgingData.setStayDuration(1);
        lodgingData.setPreferredCustomer(true);

        Transaction response = card.authorize(new BigDecimal("48"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    Additional / Extra Charges - SALES
     */

    @Test
    public void lodging_045_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Restaurant);

        Transaction response = track.charge(new BigDecimal("49"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_046_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.GiftShop)
                .addExtraCharge(ExtraChargeType.MiniBar)
                .addExtraCharge(ExtraChargeType.Telephone);

        Transaction response = track.charge(new BigDecimal("50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_047_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Laundry);

        Transaction response = track.charge(new BigDecimal("51"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_045a_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("49"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Restaurant);

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("49"))
                .withCurrency("USD")
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    @Test
    public void lodging_046a_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal("50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.GiftShop)
                .addExtraCharge(ExtraChargeType.MiniBar)
                .addExtraCharge(ExtraChargeType.Telephone);

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("50"))
                .withCurrency("USD")
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    @Test
    public void lodging_047a_SaleSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        Transaction response = track.charge(new BigDecimal("51"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Laundry);

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("51"))
                .withCurrency("USD")
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    /*
    Additional / Extra Charges - AUTHORIZATIONS
     */

    @Test
    public void lodging_048_AuthSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.authorize(new BigDecimal("49"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Restaurant);

        Transaction captureResponse = response.capture()
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void lodging_049_AuthSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.authorize(new BigDecimal("50"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.GiftShop)
                .addExtraCharge(ExtraChargeType.MiniBar)
                .addExtraCharge(ExtraChargeType.Telephone);

        Transaction captureResponse = response.capture()
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void lodging_050_AuthSwiped_ExtraCharges() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        Transaction response = track.authorize(new BigDecimal("51"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        LodgingData lodgingData = new LodgingData();
        lodgingData.addExtraCharge(ExtraChargeType.Laundry);

        Transaction captureResponse = response.capture()
                .withLodgingData(lodgingData)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    /*
    Partial Approvals - SALES
     */

    @Test
    public void lodging_051_SaleSwiped_PartialApproval() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal("130"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("110.00"), response.getAuthorizedAmount());
    }

    @Test
    public void lodging_052_SaleKeyed_PartialApproval() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        Transaction response = card.charge(new BigDecimal("145"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("65.00"), response.getAuthorizedAmount());
    }

    /*
    Partial Approvals - AUTHORIZATIONS
     */

    @Test
    public void lodging_053_AuthSwiped_PartialApproval() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.authorize(new BigDecimal("130"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("110.00"), response.getAuthorizedAmount());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_054_AuthKeyed_PartialApproval() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        Transaction response = card.authorize(new BigDecimal("145"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("65.00"), response.getAuthorizedAmount());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    CARD VERIFY
     */

    @Test
    public void lodging_055_VerifySwiped() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_056_VerifyKeyed() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        Transaction response = card.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_057_VerifySwiped() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        Transaction response = track.verify()
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    FORCE / VOICE AUTHORIZATIONS
     */

    @Test
    public void lodging_058_OfflineAuth() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.authorize(new BigDecimal("52"))
                .withCurrency("USD")
                .withOfflineAuthCode("654321")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    RETURN
     */

    @Test
    public void lodging_059_ReturnByCard() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, true);

        Transaction response = card.refund(new BigDecimal("53"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    RETURN by TxnID
     */

    @Test
    public void lodging_059a_ReturnByTxnId() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        // sale
        Transaction response = track.charge(new BigDecimal("53"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // refund
        Transaction refund = response.refund(new BigDecimal("53"))
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);
        assertEquals("00", refund.getResponseCode());
    }

    @Test
    public void lodging_059b_ReturnByTxnId() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        // sale
        Transaction response = card.charge(new BigDecimal("54"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // refund
        Transaction refund = response.refund(new BigDecimal("54"))
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);
        assertEquals("00", refund.getResponseCode());
    }

    /*
    LEVEL II Corporate Purchase Card - SALES
     */

    @Test
    public void lodging_060_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_061_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_062_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal("123.45"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("R", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_063_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_064_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_065_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal("111.07"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_066_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.08"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_067_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_068_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        Transaction response = track.charge(new BigDecimal("111.10"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_069_LevelII_Sale() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        Transaction response = track.charge(new BigDecimal("111.11"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_070_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.12"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    @Test
    public void lodging_071_LevelII_Sale() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.13"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());
    }

    /*
    LEVEL II Corporate Purchase Card - AUTHORIZATIONS
     */

    @Test
    public void lodging_072_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.authorize(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_073_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.authorize(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_074_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.authorize(new BigDecimal("123.45"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("R", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_075_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.authorize(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_076_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.authorize(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_077_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.authorize(new BigDecimal("111.07"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_078_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.authorize(new BigDecimal("111.08"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_079_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.authorize(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_080_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        Transaction response = track.authorize(new BigDecimal("111.10"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_081_LevelII_Authorization() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe();

        Transaction response = track.authorize(new BigDecimal("111.11"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_082_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.authorize(new BigDecimal("111.12"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.NotUsed)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_083_LevelII_Authorization() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.authorize(new BigDecimal("111.13"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction edit = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .withPoNumber("9876543210")
                .execute();
        assertNotNull(edit);
        assertEquals("00", edit.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    Incremental Authorizations - SALES
     */

    @Test
    public void lodging_084_IncrementalAuth_Sale() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("115"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction incremental = response.increment(new BigDecimal("23"))
                .withCurrency("USD")
                .execute();
        assertNotNull(incremental);
        assertEquals("00", incremental.getResponseCode());
    }

    @Test
    public void lodging_084_IncrementalAuth_SaleWithCOF() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal("115"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction nextResponse = card.charge(new BigDecimal("115"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                .execute();
        assertNotNull(nextResponse);
        assertEquals("00", nextResponse.getResponseCode());

        Transaction incremental = nextResponse.increment(new BigDecimal("23"))
                .withCurrency("USD")
                .execute();
        assertNotNull(incremental);
        assertEquals("00", incremental.getResponseCode());
    }

    @Test
    public void lodging_085_IncrementalAuth_Sale() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.charge(new BigDecimal("116"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction incremental = response.increment(new BigDecimal("24"))
                .withCurrency("USD")
                .execute();
        assertNotNull(incremental);
        assertEquals("00", incremental.getResponseCode());
    }

    /*
    Incremental Authorizations - AUTHORIZATIONS
     */

    @Test
    public void lodging_086_IncrementalAuth_Auth() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.authorize(new BigDecimal("115"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction incremental = response.increment(new BigDecimal("23"))
                .withCurrency("USD")
                .execute();
        assertNotNull(incremental);
        assertEquals("00", incremental.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void lodging_087_IncrementalAuth_Auth() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        Transaction response = track.authorize(new BigDecimal("116"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction incremental = response.increment(new BigDecimal("24"))
                .withCurrency("USD")
                .execute();
        assertNotNull(incremental);
        assertEquals("00", incremental.getResponseCode());

        // capture
        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

    /*
    ONLINE VOID / REVERSAL (Required)
     */

    @Test
    public void lodging_088_OnlineVoid() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("122"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = response.voidTransaction().execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_089_OnlineVoid() throws ApiException {
        CreditCardData track = TestCards.MasterCardManual(true, true);

        Transaction response = track.charge(new BigDecimal("124"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = response.voidTransaction().execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_090_OnlineVoid() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(false, true);

        Transaction response = card.charge(new BigDecimal("125"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = response.voidTransaction().execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_091_OnlineVoid() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        Transaction response = track.charge(new BigDecimal("155"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("100.00"), response.getAuthorizedAmount());

        Transaction reversal = response.reverse(new BigDecimal("100")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    /*
    ONLINE VOID / REVERSAL FOR INCREMENTALS
     */

    @Test
    public void lodging_092_IncrementalReversal() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        Transaction response = track.charge(new BigDecimal("126"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction firstIncrement = response.increment(new BigDecimal("26"))
                .withCurrency("USD")
                .execute();
        assertNotNull(firstIncrement);
        assertEquals("00", firstIncrement.getResponseCode());

        Transaction secondIncrement = response.increment(new BigDecimal("31"))
                .withCurrency("USD")
                .execute();
        assertNotNull(secondIncrement);
        assertEquals("00", secondIncrement.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal("126")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_093_IncrementalReversal() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        Transaction response = track.charge(new BigDecimal("127"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction firstIncrement = response.increment(new BigDecimal("27"))
                .withCurrency("USD")
                .execute();
        assertNotNull(firstIncrement);
        assertEquals("00", firstIncrement.getResponseCode());

        Transaction secondIncrement = response.increment(new BigDecimal("32"))
                .withCurrency("USD")
                .execute();
        assertNotNull(secondIncrement);
        assertEquals("00", secondIncrement.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal("127")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    /*
    PIN DEBIT CARD FUNCTIONS
    SALE
     */

    @Test
    public void lodging_094_DebitSale() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = track.charge(new BigDecimal("139"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_095_DebitSale() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.MasterCardSwipe(), "F505AD81659AA42A3D123412324000AB");

        Transaction response = track.charge(new BigDecimal("135"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("5"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    PARTIALLY - APPROVED PURCHASE
     */

    @Test
    public void lodging_096_DebitPartialApproval() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = track.charge(new BigDecimal("33"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("22.00"), response.getAuthorizedAmount());
    }

    /*
    RETURN
     */

    @Test
    public void lodging_097_DebitReturn() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = track.refund(new BigDecimal("141"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    ONLINE VOID / REVERSAL (Required)
     */

    @Test
    public void lodging_098_DebitReversal() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = track.charge(new BigDecimal("142"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversal = track.reverse(new BigDecimal("142"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_099_DebitReversal() throws ApiException {
        DebitTrackData track = TestCards.asDebit(TestCards.MasterCardSwipe(), "F505AD81659AA42A3D123412324000AB");

        Transaction response = track.charge(new BigDecimal("44"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());

        Transaction reversal = track.reverse(new BigDecimal("44"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    /*
    CONTACTLESS - Sales
     */

    @Test
    public void lodging_100_ContactlessSale() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe(EntryMethod.Proximity);

        Transaction response = track.charge(new BigDecimal("6"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_101_ContactlessSale() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe(EntryMethod.Proximity);

        Transaction response = track.charge(new BigDecimal("6"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_102_ContactlessSale() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe(EntryMethod.Proximity);

        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void lodging_103_ContactlessSale() throws ApiException {
        CreditTrackData track = TestCards.AmexSwipe(EntryMethod.Proximity);

        Transaction response = track.charge(new BigDecimal("9"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /*
    TIME OUT REVERSAL (TOR)
     */

    @Test
    public void lodging_104_TimeOutReversal() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        String clientTransactionId = (10000000 + new Random().nextInt(90000000)) + "";

        track.charge(new BigDecimal("15.27"))
            .withCurrency("USD")
            .withClientTransactionId(clientTransactionId)
            .execute();

        Transaction reversal = track.reverse(new BigDecimal("15.27"))
                .withCurrency("USD")
                .withClientTransactionId(clientTransactionId)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_105_TimeOutReversal() throws ApiException {
        CreditTrackData track = TestCards.MasterCardSwipe();

        String clientTransactionId = (10000000 + new Random().nextInt(90000000)) + "";

        track.charge(new BigDecimal("15.28"))
                .withCurrency("USD")
                .withClientTransactionId(clientTransactionId)
                .execute();

        Transaction reversal = track.reverse(new BigDecimal("15.28"))
                .withCurrency("USD")
                .withClientTransactionId(clientTransactionId)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_106_TimeOutReversal() throws ApiException {
        CreditTrackData track = TestCards.DiscoverSwipe();

        String clientTransactionId = (10000000 + new Random().nextInt(90000000)) + "";

        track.charge(new BigDecimal("15.29"))
                .withCurrency("USD")
                .withClientTransactionId(clientTransactionId)
                .execute();

        Transaction reversal = track.reverse(new BigDecimal("15.29"))
                .withCurrency("USD")
                .withClientTransactionId(clientTransactionId)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void lodging_999_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            TestCase.assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (!exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                Assert.fail(exc.getMessage());
        }
    }
}
