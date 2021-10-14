package com.global.api.tests.genius;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.MobilePaymentMethodType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GeniusConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class GeniusCreditTests {
    private Address address;

    private CreditCardData applePay;
    private CreditCardData card;
    private CreditCardData tokenizedCard;
    private CreditTrackData track;

    public GeniusCreditTests() throws ApiException {
		GeniusConfig config = new GeniusConfig();
        config.setMerchantName("Test Shane Logsdon");
        config.setMerchantSiteId("BKHV2T68");
        config.setMerchantKey("AT6AN-ALYJE-YF3AW-3M5NN-UQDG1");
        config.setRegisterNumber("35");
        config.setTerminalId("3");
        config.setEnvironment(Environment.TEST);
		config.setEnableLogging(true);
        ServicesContainer.configureService(config);

        address = new Address();
        address.setStreetAddress1("1 Federal Street");
        address.setPostalCode("02110");

        // TODO: Get Valid ApplePay Token
        applePay = new CreditCardData();
        applePay.setToken("ew0KCSJ2ZXJzaW9uIjogIkVDX3YxIiwNCgkiZ");
        applePay.setMobileType(MobilePaymentMethodType.APPLEPAY);

        card = TestCards.VisaManual();

        tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("100000101GC58TDAUFDZ");

        track = TestCards.VisaSwipe();
    }

	@Test
    public void AdjustTip() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10.00"))
            .withCurrency("USD")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction adjust = response.edit()
            .withGratuity(new BigDecimal("1.00"))
            .execute();
        assertNotNull(adjust);
        assertEquals("00", adjust.getResponseCode());
    }

	@Test
    public void Authorize_Keyed() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("10.00"))
            .withCurrency("USD")
            .withAddress(address)
            .withInvoiceNumber("1556")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Authorize_Swiped() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("10.00"))
            .withCurrency("USD")
            .withInvoiceNumber("1264")
            .withClientTransactionId("137149")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Authorize_Vault() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);

        Transaction response = tokenizedCard.authorize(new BigDecimal("10.00"))
            .withCurrency("USD")
            .withInvoiceNumber("1558")
            .withClientTransactionId("167903")
            .withStoredCredential(storedCredential)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void BoardCard_Keyed() throws ApiException {
        String token = card.tokenize();
        assertNotNull(token);
    }

	@Test
    public void BoardCard_Swiped() throws ApiException {
        String token = track.tokenize();
        assertNotNull(token);
    }

    @Test
    public void BoardCard_Vault() throws ApiException {
        // TODO: for use with single use tokens
        String token = tokenizedCard.tokenize();
        assertNotNull(token);
    }

	@Test
    public void Capture() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("10.00"))
            .withCurrency("USD")
            .withInvoiceNumber("1264")
            .withClientTransactionId("137149")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction capture = response.capture().execute();
        assertNotNull(capture);
        assertEquals("00", capture.getResponseCode());
    }

	@Test
    public void UpdateBoardedCard() throws ApiException {
        tokenizedCard.setExpMonth(12);
        tokenizedCard.setExpYear(2025);

        boolean success = tokenizedCard.updateTokenExpiry();
        assertTrue(success);
    }

	@Test
    public void ForceCapture_Keyed() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("10.00"))
            .withCurrency("USD")
            .withOfflineAuthCode("V00546")
            .withInvoiceNumber("1559")
            .withClientTransactionId("168901")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Refund_Keyed() throws ApiException {
        // TODO: There is some weirdness around adding the client transaction id to a refund by card.
        Transaction response = card.refund(new BigDecimal("4.01"))
            .withCurrency("USD")
            .withInvoiceNumber("1701")
            //.withClientTransactionId("165901")
            .withAddress(address)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Refund_TransactionId() throws ApiException {
        Transaction response = card.charge(new BigDecimal("4.01"))
            .withCurrency("USD")
            .withInvoiceNumber("1703")
            .withClientTransactionId("165902")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // TODO: ClientTransactionId not implemented on manage transactions (assumed same as original)
        Transaction refund = response.refund()
            .withInvoiceNumber("1703")
            //.withClientTransactionId("165902")
            .execute();
        assertNotNull(refund);
        assertEquals("00", refund.getResponseCode());
    }

	@Test
    public void Refund_Swiped() throws ApiException {
        // TODO: There is some weirdness around adding the client transaction id to a refund by card.
        Transaction response = track.refund(new BigDecimal("4.01"))
            .withCurrency("USD")
            .withInvoiceNumber("1701")
            //.withClientTransactionId("165901")
            .withAddress(address)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Refund_Vault() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.Merchant);

        // TODO: There is some weirdness around adding the client transaction id to a refund by card.
        Transaction response = tokenizedCard.refund(new BigDecimal("1.83"))
            .withCurrency("USD")
            .withCashBack(new BigDecimal("0"))
            //.withTaxAmount(new BigDecimal("0"))
            .withInvoiceNumber("1559")
            //PoNumber
            //CustomerCode
            //.withClientTransactionId("166909")
            .withAllowPartialAuth(false)
            .withAllowDuplicates(false)
            .withStoredCredential(storedCredential)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Sale_Keyed() throws ApiException {
        Transaction response = card.charge(new BigDecimal("1.05"))
            .withCurrency("USD")
            .withCashBack(new BigDecimal("0"))
            //.withTaxAmount(new BigDecimal("0"))
            .withInvoiceNumber("12345")
            // PoNumber
            // CustomerCode
            .withClientTransactionId("166901")
            .withAllowPartialAuth(false)
            .withAllowDuplicates(false)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Sale_Swiped() throws ApiException {
        Transaction response = track.charge(new BigDecimal("1.29"))
            .withCurrency("USD")
            .withCashBack(new BigDecimal("0"))
            //.withTaxAmount(new BigDecimal("0"))
            .withInvoiceNumber("12345")
            // PoNumber
            // CustomerCode
            .withClientTransactionId("138401")
            .withAllowPartialAuth(false)
            .withAllowDuplicates(false)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void Sale_Vault() throws ApiException {
        StoredCredential storedCredential = new StoredCredential();
        storedCredential.setInitiator(StoredCredentialInitiator.CardHolder);

        Transaction response = tokenizedCard.charge(new BigDecimal("1.29"))
             .withCurrency("USD")
             .withCashBack(new BigDecimal("0"))
             //.withTaxAmount(new BigDecimal("0"))
             .withInvoiceNumber("1559")
             // PoNumber
             // CustomerCode
             .withClientTransactionId("166909")
             .withAllowPartialAuth(false)
             .withAllowDuplicates(false)
             .withStoredCredential(storedCredential)
             .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void Sale_Wallet() throws ApiException {
        Transaction response = applePay.charge()
            .withCurrency("USD")
            .withCashBack(new BigDecimal("1.00"))
            //.withTaxAmount(new BigDecimal("1.00"))
            .withInvoiceNumber("INV123")
            // PoNumber
            // CustomerCode
            .withClientTransactionId("TX123")
             .withAllowPartialAuth(true)
             .withAllowDuplicates(true)
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

	@Test
    public void SettleBatch() throws ApiException {
        BatchSummary response = BatchService.closeBatch();
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
    }

	@Test
    public void UnboardCard() throws ApiException {
        String token = TestCards.MasterCardManual().tokenize();
        assertNotNull(token);

        CreditCardData deleteCard = new CreditCardData();
        deleteCard.setToken(token);

        boolean success = deleteCard.deleteToken("default");
        assertTrue(success);
    }

	@Test
    public void Void() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10.00"))
            .withCurrency("USD")
            .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
}
