package com.global.api.tests.certifications;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.*;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RetailCertification {
    private boolean useTokens = false;

    private String visa_token;
    private String mastercard_token;
    private String amex_token;
    
    public RetailCertification() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configure(config);
    }

    @Test
    public void retail_000_CloseBatch() throws ApiException {
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
            CREDIT CARD FUNCTIONS
            CARD VERIFY
            ACCOUNT VERIFICATION
         */

    @Test
    public void retail_001_CardverifyVisa() throws ApiException {
        CreditTrackData visa_enc = TestCards.VisaSwipeEncrypted();

        Transaction response = visa_enc.verify()
                .withRequestMultiUseToken(useTokens)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        if (useTokens) {
            assertNotNull(response.getToken(), "token is null");

            CreditCardData token = new CreditCardData(response.getToken());
            Transaction saleResponse = token.charge(new BigDecimal("15.01"))
                    .withAllowDuplicates(true)
                    .execute();
            assertNotNull(saleResponse);
            assertEquals("00", saleResponse.getResponseCode());
        }
    }

    @Test
    public void retail_002_CardverifyMastercardSwipe() throws ApiException {
        CreditTrackData card_enc = TestCards.MasterCardSwipeEncrypted();

        Transaction response = card_enc.verify()
                .withRequestMultiUseToken(useTokens)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        if (useTokens) {
            assertNotNull(response.getToken());

            CreditCardData token = new CreditCardData(response.getToken());

            Transaction saleResponse = token.charge(new BigDecimal("15.02"))
                    .withAllowDuplicates(true)
                    .execute();
            assertNotNull(saleResponse);
            assertEquals("00", saleResponse.getResponseCode());
        }
    }

    @Test
    public void retail_003_CardverifyDiscover() throws ApiException {
        CreditTrackData discover_enc = TestCards.DiscoverSwipeEncrypted();
        Transaction response = discover_enc.verify()
                .withRequestMultiUseToken(useTokens)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        if (useTokens) {
            assertNotNull(response.getToken());

            CreditCardData token = new CreditCardData(response.getToken());
            Transaction saleResponse = token.charge(new BigDecimal("15.03"))
                    .withAllowDuplicates(true)
                    .execute();
            assertNotNull(saleResponse);
            assertEquals("00", saleResponse.getResponseCode());
        }
    }

    // Address Verification

    @Test
    public void retail_004_CardverifyAmex() throws ApiException {
        Address address = new Address("75024");

        CreditCardData manual_amex = TestCards.AmexManual(false, true);

        Transaction response = manual_amex.verify()
                .withAddress(address)
                .withRequestMultiUseToken(useTokens)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        if (useTokens) {
            assertNotNull(response.getToken());

            CreditCardData token = new CreditCardData(response.getToken());
            Transaction saleResponse = token.charge(new BigDecimal("15.04"))
                    .withAllowDuplicates(true)
                    .execute();
            assertNotNull(saleResponse);
            assertEquals("00", saleResponse.getResponseCode());
        }
    }

    // Balance Inquiry (for Prepaid)

    @Test
    public void retail_005_BalanceInquiryVisa() throws ApiException {
        CreditTrackData visa_enc = TestCards.VisaSwipeEncrypted();

        Transaction response = visa_enc.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // CREDIT SALE (For multi-use token only)

    @Test
    public void retail_006_chargeVisaSwipeToken() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();
        Transaction response = card.charge(new BigDecimal("15.01"))
                .withCurrency("USD")
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        visa_token = response.getToken();
    }

    @Test
    public void retail_007_chargeMastercardSwipeToken() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();
        Transaction response = card.charge(new BigDecimal("15.02"))
                .withCurrency("USD")
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        mastercard_token = response.getToken();
    }

    @Test
    public void retail_008_chargeDiscoverSwipeToken() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();
        Transaction response = card.charge(new BigDecimal("15.03"))
                .withCurrency("USD")
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        //discover_token = response.getToken();
    }

    @Test
    public void retail_009_chargeAmexSwipeToken() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe();
        Transaction response = card.charge(new BigDecimal("15.04"))
                .withCurrency("USD")
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        amex_token = response.getToken();
    }

        /*
            CREDIT SALE
            SWIPED
         */

    @Test
    public void retail_010_chargeVisaSwipe() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();
        Transaction response = card.charge(new BigDecimal("15.01"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test 59
        Transaction reverse = response.reverse(new BigDecimal("15.01")).execute();
        assertNotNull(reverse);
        assertEquals("00", reverse.getResponseCode());
    }

    @Test
    public void retail_011_chargeMastercardSwipe() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();
        Transaction response = card.charge(new BigDecimal("15.02"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_012_chargeDiscoverSwipe() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();
        Transaction response = card.charge(new BigDecimal("15.03"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_013_chargeAmexSwipe() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe();
        Transaction response = card.charge(new BigDecimal("15.04"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_014_chargeJbcSwipe() throws ApiException {
        CreditTrackData card = TestCards.JcbSwipe();
        Transaction response = card.charge(new BigDecimal("15.05"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 58
        Transaction refund = response.refund(new BigDecimal("15.05"))
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);
        assertEquals("00", refund.getResponseCode());
    }

    @Test
    public void retail_014_chargeRetailMastercard24() throws ApiException {
        CreditTrackData card = TestCards.MasterCard24Swipe();
        Transaction response = card.charge(new BigDecimal("15.34"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_014_chargeRetailMastercard25() throws ApiException {
        CreditTrackData card = TestCards.MasterCard25Swipe();
        Transaction response = card.charge(new BigDecimal("15.34"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_015_chargeVisaSwipe() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();
        Transaction response = card.charge(new BigDecimal("15.06"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 63
        Transaction reversal = response.reverse(new BigDecimal("15.06")).withAuthAmount(new BigDecimal("5.06")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    // Manually Entered - Card Present

    @Test
    public void retail_016_chargeVisaManualCardPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData manual_card = TestCards.VisaManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.01"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_017_chargeMasterCardManualCardPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData manual_card = TestCards.MasterCardManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.02"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 60
        Transaction reverse = response.reverse(new BigDecimal("16.02")).execute();
        assertNotNull(reverse);
        assertEquals("00", reverse.getResponseCode());
    }

    @Test
    public void retail_018_chargeDiscoverManualCardPresent() throws ApiException {
        Address address = new Address("750241234");

        CreditCardData manual_card = TestCards.DiscoverManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.03"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_019_chargeAmexManualCardPresent() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData manual_card = TestCards.AmexManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.04"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_020_chargeJcbManualCardPresent() throws ApiException {
        Address address = new Address("75024");

        CreditCardData manual_card = TestCards.JcbManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.05"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_021_chargeDiscoverManualCardPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData manual_card = TestCards.DiscoverManual(true, true);
        Transaction response = manual_card.charge(new BigDecimal("16.07"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 64
        Transaction reversal = response.reverse(new BigDecimal("16.07"))
                .withAuthAmount(new BigDecimal("6.07"))
                .execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    // Manually Entered - Card Not Present

    @Test
    public void retail_022_chargeVisaManualCardNotPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData manual_card = useTokens ? new CreditCardData(visa_token) : TestCards.VisaManual(false, true);

        Transaction response = manual_card.charge(new BigDecimal("17.01"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_023_chargeMasterCardManualCardNotPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData manual_card = useTokens ? new CreditCardData(mastercard_token) : TestCards.MasterCardManual(false, true);

        Transaction response = manual_card.charge(new BigDecimal("17.02"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 61
        Transaction reversal = response.reverse(new BigDecimal("17.02")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void retail_024_chargeDiscoverManualCardNotPresent() throws ApiException {
        Address address = new Address("750241234");

        CreditCardData manual_card = useTokens ? new CreditCardData(mastercard_token) : TestCards.DiscoverManual(false, true);

        Transaction response = manual_card.charge(new BigDecimal("17.03"))
                .withCurrency("USD").withAddress(address).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_025_chargeAmexManualCardNotPresent() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData manual_card = useTokens ? new CreditCardData(amex_token) : TestCards.AmexManual(false, true);

        Transaction response = manual_card.charge(new BigDecimal("17.04"))
                .withCurrency("USD").withAddress(address).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_026_chargeJcbManualCardNotPresent() throws ApiException {
        Address address = new Address("75024");

        CreditCardData manual_card = TestCards.JcbManual(false, true);
        Transaction response = manual_card.charge(new BigDecimal("17.05"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Contactless

    @Test
    public void retail_027_chargeVisaContactless() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe(EntryMethod.Proximity);
        Transaction response = card.charge(new BigDecimal("18.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_028_chargeMastercardContactless() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe(EntryMethod.Proximity);

        Transaction response = card.charge(new BigDecimal("18.02"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_029_chargeDiscoverContactless() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe(EntryMethod.Proximity);

        Transaction response = card.charge(new BigDecimal("18.03"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_030_chargeAmexContactless() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe(EntryMethod.Proximity);

        Transaction response = card.charge(new BigDecimal("18.04"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // AUTHORIZATION

    @Test
    public void retail_031_AuthorizeVisaSwipe() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        // 031a authorize
        Transaction response = card.authorize(new BigDecimal("15.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 031b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_032_AuthorizeVisaSwipeAdditionalAuth() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        // 032a authorize
        Transaction response = card.authorize(new BigDecimal("15.09"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 032b Additional Auth (restaurant only)

        // 032c Add to batch
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_033_AuthorizeMasterCardSwipe() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();

        // 033a authorize
        Transaction response = card.authorize(new BigDecimal("15.10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 033b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_033__AuthorizeDiscoverSwipe() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();

        Transaction response = card.authorize(new BigDecimal("15.10"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // AUTHORIZATION - Manually Entered, Card Present

    @Test
    public void retail_034_AuthorizeVisaManualCardPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.VisaManual(true, true);

        // 034a authorize
        Transaction response = card.authorize(new BigDecimal("16.08"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 034b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_035_AuthorizeVisaManualCardPresentAdditionalAuth() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.VisaManual(true, true);

        // 035a authorize
        Transaction response = card.authorize(new BigDecimal("16.09"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 035b Additional Auth (restaurant only)

        // 035c Add to batch
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_036_AuthorizeMasterCardManualCardPresent() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "75024");

        CreditCardData card = TestCards.MasterCardManual(true, true);

        // 036a authorize
        Transaction response = card.authorize(new BigDecimal("16.10"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 036b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_036__AuthorizeDiscoverManualCardPresent() throws ApiException {
        Address address = new Address("750241234");

        CreditCardData card = TestCards.DiscoverManual(true, true);
        Transaction response = card.authorize(new BigDecimal("16.10"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // AUTHORIZATION - Manually Entered, Card Not Present

    @Test
    public void retail_037_AuthorizeVisaManual() throws ApiException {
        Address address = new Address("6860 Dallas Pkwy", "750241234");

        CreditCardData card = TestCards.VisaManual(false, true);

        // 034a authorize
        Transaction response = card.authorize(new BigDecimal("17.08"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 034b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_038_AuthorizeMasterCardManual() throws ApiException {
        Address address = new Address("6860", "75024");

        CreditCardData card = TestCards.MasterCardManual(false, true);

        // 036a authorize
        Transaction response = card.authorize(new BigDecimal("17.09"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // 036b capture
        Transaction captureResponse = response.capture().execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void retail_038__AuthorizeDiscoverManual() throws ApiException {
        Address address = new Address("750241234");

        CreditCardData card = TestCards.DiscoverManual(false, true);

        Transaction response = card.authorize(new BigDecimal("17.10"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // PARTIALLY APPROVED SALE (Required)

    @Test
    public void retail_039_chargeDiscoverSwipePartialApproval() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();

        Transaction response = card.charge(new BigDecimal("40.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("40.00"), response.getAuthorizedAmount());
    }

    @Test
    public void retail_040_chargeVisaSwipePartialApproval() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();
        Transaction response = card.charge(new BigDecimal("130.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("110.00"), response.getAuthorizedAmount());
    }

    @Test
    public void retail_041_chargeDiscoverManualPartialApproval() throws ApiException {
        Address address = new Address("75024");
        CreditCardData card = TestCards.DiscoverManual(true, true);

        Transaction response = card.charge(new BigDecimal("145.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("65.00"), response.getAuthorizedAmount());
    }

    @Test
    @Ignore
    public void retail_042_chargeMasterCardSwipePartialApproval() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();
        Transaction response = card.charge(new BigDecimal("155.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("100.00"), response.getAuthorizedAmount());

        // test case 62
        Transaction reversal = response.reverse(new BigDecimal("100.00")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

        /*
            SALE WITH GRATUITY
            Tip Edit (Tip at Settlement)
         */

    @Test
    public void retail_043_chargeVisaSwipeEditGratuity() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();
        Transaction response = card.charge(new BigDecimal("15.12"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("18.12"))
                .withGratuity(new BigDecimal("3.00"))
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    @Test
    public void retail_044_chargeMasterCardManualEditGratuity() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.MasterCardManual(true, true);
        Transaction response = card.charge(new BigDecimal("15.13"))
                .withCurrency("USD")
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("18.13"))
                .withGratuity(new BigDecimal("3.00"))
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    // Tip on Purchase

    @Test
    public void retail_045_chargeVisaManualGratuity() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal("18.61"))
                .withCurrency("USD")
                .withAddress(address)
                .withGratuity(new BigDecimal("3.50"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_046_chargeMasterCardSwipeGratuity() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();

        Transaction response = card.charge(new BigDecimal("18.62"))
                .withCurrency("USD")
                .withGratuity(new BigDecimal("3.50"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction editResponse = response.edit()
                .withAmount(new BigDecimal("18.12"))
                .withGratuity(new BigDecimal("3.00"))
                .execute();
        assertNotNull(editResponse);
        assertEquals("00", editResponse.getResponseCode());
    }

    // LEVEL II CORPORATE PURCHASE CARD

    @Test
    public void retail_047_LevelIIVisaSwipeResponseB() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        Transaction response = card.charge(new BigDecimal("112.34"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    @Ignore
    public void retail_047__LevelIIVisaSwipeResonseB() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        Transaction response = card.charge(new BigDecimal("112.35"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("B", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit().withTaxType(TaxType.NotUsed).execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_048_LevelIIVisaSwipeResponseR() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        Transaction response = card.charge(new BigDecimal("123.45"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("R", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_049_LevelIIVisaManualResponseS() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal("134.56"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_050_LevelIIMasterCardSwipeResponseS() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();

        Transaction response = card.charge(new BigDecimal("111.06"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.NotUsed)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_051_LevelIIMasterCardManualResponseS() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.07"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_051__LevelIIMasterCardManualResponseS() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.MasterCardManual(true, true);
        Transaction response = card.charge(new BigDecimal("111.08"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_052_LevelIIMasterCardManualResponseS() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.MasterCardManual(true, true);
        Transaction response = card.charge(new BigDecimal("111.09"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("S", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_053_LevelIIAmexSwipeNoResponse() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe();
        Transaction response = card.charge(new BigDecimal("111.10"))
                .withCurrency("USD")
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withTaxType(TaxType.SalesTax)
                .withTaxAmount(new BigDecimal("1"))
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_054_LevelIIAmexManualNoResponse() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.charge(new BigDecimal("111.11"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.NotUsed)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_055_LevelIIAmexManualNoResponse() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.AmexManual(true, true);
        Transaction response = card.charge(new BigDecimal("111.12"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.NotUsed)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    @Test
    public void retail_055__LevelIIAmexManualNoResponse() throws ApiException {
        Address address = new Address("75024");

        CreditCardData card = TestCards.AmexManual(true, true);
        Transaction response = card.charge(new BigDecimal("111.13"))
                .withCurrency("USD")
                .withAddress(address)
                .withCommercialRequest(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("0", response.getCommercialIndicator());

        Transaction cpcResponse = response.edit()
                .withPoNumber("9876543210")
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(cpcResponse);
        assertEquals("00", cpcResponse.getResponseCode());
    }

    // OFFLINE SALE / AUTHORIZATION

    @Test
    public void retail_056_OfflineChargeVisaManual() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, true);

        Transaction response = card.charge(new BigDecimal("15.12"))
                .withCurrency("USD")
                .withOfflineAuthCode("654321")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_056_OfflineAuthVisaManual() throws ApiException {
        CreditCardData card = TestCards.VisaManual(false, true);

        Transaction response = card.authorize(new BigDecimal("15.11"))
                .withCurrency("USD")
                .withOfflineAuthCode("654321")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // RETURN

    @Test
    public void retail_057_ReturnMasterCard() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(false, true);

        Transaction response = card.refund(new BigDecimal("15.11"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_057_ReturnMasterCardSwipe() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();
        Transaction response = card.refund(new BigDecimal("15.15"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // ONLINE VOID / REVERSAL (Required)

    // PIN DEBIT CARD FUNCTIONS

    @Test
    public void retail_065_DebitSaleVisaSwipe() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("14.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_066_DebitSaleMasterCardSwipe() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.MasterCardSwipe(), "F505AD81659AA42A3D123412324000AB");

        Transaction response = card.charge(new BigDecimal("14.02"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 71
        Transaction reversal = card.reverse(new BigDecimal("14.02")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    @Test
    public void retail_067_DebitSaleVisaSwipeCashBack() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("14.03"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("5.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_067__DebitSaleMasterCard() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.MasterCardSwipe(), "F505AD81659AA42A3D123412324000AB");

        Transaction response = card.charge(new BigDecimal("14.04"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // PARTIALLY APPROVED PURCHASE

    @Test
    public void retail_068_DebitSaleMasterCardPartialApproval() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.MasterCardSwipe(), "F505AD81659AA42A3D123412324000AB");

        Transaction response = card.charge(new BigDecimal("33.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("22.00"), response.getAuthorizedAmount());
    }

    @Test
    public void retail_069_DebitSaleVisaPartialApproval() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("44.00"))
                .withCurrency("USD")
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        assertEquals("10", response.getResponseCode());
        assertEquals(new BigDecimal("33.00"), response.getAuthorizedAmount());

        // test case 72
        Transaction reversal = card.reverse(new BigDecimal("33.00")).execute();
        assertNotNull(reversal);
        assertEquals("00", reversal.getResponseCode());
    }

    // RETURN

    @Test
    public void retail_070_DebitReturnVisaSwipe() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.refund(new BigDecimal("14.07"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_070__DebitReturnVisaSwipe() throws ApiException {
        DebitTrackData card = TestCards.asDebit(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.refund(new BigDecimal("14.08"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reversalResponse = card.reverse(new BigDecimal("14.08")).execute();
        assertNotNull(reversalResponse);
        assertEquals("00", reversalResponse.getResponseCode());
    }

    // REVERSAL

        /*
           EBT FUNCTIONS
            Food Stamp Purchase
         */

    @Test
    public void retail_080_EbtfsPurchaseVisaSwipe() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipe(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("101.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_081_EbtfsPurchaseVisaManual() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("102.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Food Stamp Electronic Voucher (Manual Entry Only)

    @Test
    public void retail_082_EbtVoucherPurchaseVisa() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");
        card.setSerialNumber("123456789012345");
        card.setApprovalCode("123456");

        Transaction response = card.charge(new BigDecimal("103.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Food Stamp Balance Inquiry

    @Test
    public void retail_083_EbtfsReturnVisaSwipe() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.refund(new BigDecimal("104.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_084_EbtfsReturnVisaManual() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.refund(new BigDecimal("105.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Food Stamp Balance Inquiry

    @Test
    public void retail_085_EbtBalanceInquiryVisaSwipe() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_086_EbtBalanceInquiryVisaManual() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(true, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        /*
            assertEquals("00", response.getResponseCode());
            EBT Cash BENEFITS
            Cash Back Purchase
         */

    @Test
    public void retail_087_EbtCashBackPurchaseVisaSwipe() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("106.01"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("5.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_088_EbtCashBackPurchaseVisaManual() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("107.01"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("5.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // No Cash Back Purchase

    @Test
    public void retail_089_EbtCashBackPurchaseVisaSwipeNoCashBack() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("108.01"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("0"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_090_EbtCashBackPurchaseVisaManualNoCashBack() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("109.01"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("0"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Cash Back Balance Inquiry

    @Test
    public void retail_091_EbtBalanceInquiryVisaSwipeCash() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.balanceInquiry(InquiryType.Cash).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_092_EbtBalanceInquiryVisaManualCash() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(true, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.balanceInquiry(InquiryType.Cash).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Bash Benefits.withdrawal

    @Test
    public void retail_093_EbtBenefitWithDrawalVisaSwipe() throws ApiException {
        EBTTrackData card = TestCards.asEBT(TestCards.VisaSwipeEncrypted(), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("110.01"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_094_EbtBenefitWithDrawalVisaManual() throws ApiException {
        EBTCardData card = TestCards.asEBT(TestCards.VisaManual(false, true), "32539F50C245A6A93D123412324000AA");

        Transaction response = card.charge(new BigDecimal("111.01"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("0"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

        /*
            HMS GIFT - REWARDS
            GIFT
            ACTIVATE
         */

    @Test
    public void retail_095_ActivateGift1Swipe() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.activate(new BigDecimal("6.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_096_ActivateGift2Manual() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.activate(new BigDecimal("7.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // ADD VALUE

    @Test
    public void retail_097_AddValueGift1Swipe() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.addValue(new BigDecimal("8.00"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_098_AddValueGift2Manual() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.activate(new BigDecimal("9.00")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // BALANCE INQUIRY

    @Test
    public void retail_099_BalanceInquiryGift1Swipe() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    @Test
    public void retail_100_BalanceInquiryGift2Manual() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10.00"), response.getBalanceAmount());
    }

    // REPLACE / TRANSFER

    @Test
    public void retail_101_ReplaceGift1Swipe() throws ApiException {
        GiftCard oldCard = TestCards.GiftCard1Swipe();
        GiftCard newCard = TestCards.GiftCard2Manual();

        Transaction response = oldCard.replaceWith(newCard).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_102_ReplaceGift2Manual() throws ApiException {
        GiftCard newCard = TestCards.GiftCard1Swipe();
        GiftCard oldCard = TestCards.GiftCard2Manual();

        Transaction response = oldCard.replaceWith(newCard).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // SALE / REDEEM

    @Test
    public void retail_103_SaleGift1Swipe() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.charge(new BigDecimal("1.00"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_104_SaleGift2Manual() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.charge(new BigDecimal("2.00"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_105_SaleGift1VoidSwipe() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.charge(new BigDecimal("3.00"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // test case 107
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void retail_106_SaleGift2ReversalManual() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.charge(new BigDecimal("4.00"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        //test case 108
        Transaction voidResponse = response.reverse(new BigDecimal("4.00")).execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    // VOID

    // REVERSAL

    // DEACTIVATE

    @Test
    public void retail_109_deactivateGift1() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // RECEIPTS MESSAGING

    @Test
    public void retail_110_ReceiptsMessaging() throws ApiException {
        // PRINT AND SCAN RECEIPT FOR TEST 107
    }

        /*
            REWARDS
            BALANCE INQUIRY
         */

    @Test
    public void retail_111_BalanceInquiryRewards1() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("0"), response.getPointsBalanceAmount());
    }

    @Test
    public void retail_112_BalanceInquiryRewards2() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("0"), response.getPointsBalanceAmount());
    }

    // ALIAS

    @Test
    public void retail_113_CreateAliasGift1() throws ApiException {
        GiftCard card = GiftCard.create("9725550100");
        assertNotNull(card);
    }

    @Test
    public void retail_114_CreateAliasGift2() throws ApiException {
        GiftCard card = GiftCard.create("9725550100");
        assertNotNull(card);
    }

    @Test
    public void retail_115_AddAliasGift1() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.addAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_116_AddAliasGift2() throws ApiException {
        GiftCard card = TestCards.GiftCard2Manual();

        Transaction response = card.addAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_117_DeleteAliasGift1() throws ApiException {
        GiftCard card = TestCards.GiftCard1Swipe();

        Transaction response = card.removeAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void retail_999_CloseBatch() throws ApiException {
        try {
            BatchSummary response = BatchService.closeBatch();
            TestCase.assertNotNull(response);
            System.out.println(String.format("Batch ID: %s", response.getBatchId()));
            System.out.println(String.format("Sequence Number: %s", response.getSequenceNumber()));
        }
        catch (GatewayException exc) {
            if (exc.getResponseText().contains("Transaction was rejected because it requires a batch to be open."))
                Assert.fail(exc.getMessage());
        }
    }
}
