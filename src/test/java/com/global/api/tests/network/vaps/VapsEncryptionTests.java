package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsEncryptionTests {
    private CreditCardData card;
    private CreditCardData cardWithCvn;
    private CreditTrackData track;
    private DebitTrackData debit;

    public VapsEncryptionTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // AMEX
        card = TestCards.AmexManualEncrypted();
        cardWithCvn = TestCards.AmexManualEncrypted();
        cardWithCvn.setCvn("9072488");
        track = TestCards.AmexSwipeEncrypted();

        // DISCOVER
//        card = TestCards.DiscoverManualEncrypted();
//        cardWithCvn = TestCards.DiscoverManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.DiscoverSwipeEncryptedV2();

        // MASTERCARD
//        card = TestCards.MasterCardManualEncrypted();
//        cardWithCvn = TestCards.MasterCardManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.MasterCardSwipeEncryptedV2();

        // VISA
//        card = TestCards.VisaManualEncrypted();
//        cardWithCvn = TestCards.VisaManualEncrypted();
//        cardWithCvn.setCvn("7803754");
//        cashCard = TestCards.VisaSwipeEncryptedV2();

        // DEBIT
        debit = new DebitTrackData();
        debit.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        debit.setPinBlock("62968D2481D231E1A504010024A00014");
        debit.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test
    public void test_001_credit_manual_auth_cvn() throws ApiException {
        Transaction response = cardWithCvn.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_auth() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_003_credit_manual_sale_cvn() throws ApiException {
        Transaction response = cardWithCvn.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #7
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_005_credit_manual_refund_cvn() throws ApiException {
        Transaction response = cardWithCvn.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_006_credit_manual_refund() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_034_credit_swipe_auth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_036_credit_swipe_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction test case #40
        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals(response.getResponseMessage(), "400", voidResponse.getResponseCode());

        // reverse the transaction test case #39
        Transaction reverseResponse = response.reverse().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_038_credit_swipe_refund() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
}
