package com.global.api.tests.transit;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EmvFallbackCondition;
import com.global.api.entities.enums.EmvLastChipRead;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.TransitConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransitCreditTests {
    private static final BigDecimal AMOUNT = new BigDecimal("12.35");
    private final CreditCardData card;
    private final CreditTrackData track;
    private final CreditCardData tokenizedCard;
    private final Address address;

    public TransitCreditTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        /* The following are the default values for the AcceptorConfig */

        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
        // acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Attended);
        // acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
        // acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Unknown);
        // acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);
        // acceptorConfig.setCardCaptureCapability(false);
        // acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        // acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.None);

        TransitConfig config = getConfig();
        config.setRequestLogger(new RequestConsoleLogger());
        ServicesContainer.configureService(config);

        address = new Address();
        address.setStreetAddress1("1 Federal Street");
        address.setPostalCode("02110");

        card = TestCards.VisaManual(false, false);
        card.setCvn("999");

        track = TestCards.VisaSwipe(EntryMethod.Swipe);

        tokenizedCard = new CreditCardData();
        tokenizedCard.setToken("fDBCfoLroIVk0119");
    }

    protected TransitConfig getConfig() {
        TransitConfig config = new TransitConfig();
        config.setMerchantId("884000003531");
        config.setUsername("TA5876503");
        config.setPassword("HRQATest!000");
        config.setDeviceId("88400000353102");
        config.setTransactionKey("7WDYEC6LE9T5Q8EER5CWRPN3P4O5BZH8");
        config.setDeveloperId("003226G001");
        config.setGatewayProvider(GatewayProvider.TRANSIT);
        config.setAcceptorConfig(new AcceptorConfig());
        return config;
    }

    @Test
    public void saleManual() throws ApiException {
        Transaction response = card.charge(AMOUNT)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());
    }

    @Test
    public void saleManualAmountBiggerThan1000() throws ApiException {
        BigDecimal amount = new BigDecimal("2156.56");
        Transaction response = card.charge(amount)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(amount, response.getAuthorizedAmount());
    }

    @Test
    public void refundManualAmountBiggerThan1000() throws ApiException {
        BigDecimal amount = new BigDecimal("2156.56");
        Transaction response = card.charge(amount)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(amount, response.getAuthorizedAmount());

        Transaction refundResponse = Transaction.fromId(response.getTransactionId())
                .refund(amount)
                .withCurrency("USD")
                .execute();

        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
        assertEquals(amount, response.getAuthorizedAmount());
    }

    @Test
    public void saleSwiped() throws ApiException {
        Transaction response = track.charge(AMOUNT)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());
    }

    @Test
    public void authManual() throws ApiException {
        Transaction response = card.authorize(AMOUNT)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());

        Transaction captureResponse = response.capture()
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());
    }

    @Test
    public void authSwiped() throws ApiException {
        BigDecimal amount = new BigDecimal("2156.56");
        Transaction response = track.authorize(amount)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(amount, response.getAuthorizedAmount());

        Transaction captureResponse = response.capture()
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
        assertEquals(amount, response.getAuthorizedAmount());
    }

    @Test
    @Disabled
    public void authSwipedEmvFallback() throws ApiException {
        CreditTrackData fallback = new CreditTrackData();
        fallback.setValue("5413330089010434=22122019882803290000");
        fallback.setEntryMethod(EntryMethod.Swipe);

        Transaction response = fallback.authorize(AMOUNT)
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withEmvFallbackData(EmvFallbackCondition.ChipReadFailure, EmvLastChipRead.SUCCESSFUL, "3.6.0")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());

        Transaction captureResponse = response.capture()
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
    }

    @Test
    public void authCaptureDifferentAmount() throws ApiException {
        BigDecimal amount = AMOUNT;

        Transaction response = track.authorize(amount)
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(amount.add(new BigDecimal("2")))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());
    }

    @Test
    public void authAddGratuity() throws ApiException {
        BigDecimal amount = AMOUNT;

        Transaction response = track.authorize(amount)
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(AMOUNT, response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(amount.add(new BigDecimal("2")))
                .withGratuity(new BigDecimal("2"))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
    }

    @Test
    public void authMultiCapture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("20"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(new BigDecimal("20"), response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(new BigDecimal("10"))
                .withCurrency("USD")
                .withMultiCapture(1, 2)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());

        Transaction captureResponse2 = response.capture(new BigDecimal("10"))
                .withCurrency("USD")
                .withMultiCapture(2, 2)
                .execute();
        assertNotNull(captureResponse2);
        assertEquals("00", captureResponse2.getResponseCode(), captureResponse2.getResponseMessage());
    }

    @Test
    public void authMultiCaptureMixed() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("24"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(new BigDecimal("24"), response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(new BigDecimal("12"))
                .withCurrency("USD")
                .withMultiCapture(1, 2)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());

        Transaction captureResponse2 = response.capture(new BigDecimal("12"))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse2);
        assertEquals("00", captureResponse2.getResponseCode(), captureResponse2.getResponseMessage());
    }

    @Test
    public void authMultiCaptureOverAuth() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("21"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(new BigDecimal("21"), response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(new BigDecimal("21"))
                .withCurrency("USD")
                .withMultiCapture(1, 2)
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());
        assertEquals(new BigDecimal("21"), response.getAuthorizedAmount());

        assertThrows(GatewayException.class, () -> {
            response.capture(new BigDecimal("10"))
                    .withCurrency("USD")
                    .withMultiCapture(2, 2)
                    .execute();
        });
    }

    @Test
    public void authMultiCaptureNoFlag() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("22"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertEquals(new BigDecimal("22"), response.getAuthorizedAmount());

        Transaction captureResponse = response.capture(new BigDecimal("12"))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode(), captureResponse.getResponseMessage());

        assertThrows(GatewayException.class, () -> {
            response.capture(new BigDecimal("10"))
                    .withCurrency("USD")
                    .execute();
        });
    }

    @Test
    @Disabled
    public void balanceInquirySwiped() throws ApiException {
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
        assertNotNull(response.getBalanceAmount());
    }

    @Test
    public void verifyManual() throws ApiException {
        Transaction response = card.verify().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
    }

    @Test
    public void verifySwiped() throws ApiException {
        Transaction response = track.verify().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode(), response.getResponseMessage());
    }

    @Test
    public void refundWithReference() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getAuthorizedAmount());

        Transaction refundResponse = Transaction.fromId(response.getTransactionId())
                .refund()
                .withCurrency("USD")
                .execute();

        assertNotNull(refundResponse);
        assertEquals("00", refundResponse.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getAuthorizedAmount());
    }


    @Test
    public void testAdjustTip() throws ApiException {
        Transaction response = track.charge(new BigDecimal("20"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction adjust = response.edit()
                .withGratuity(new BigDecimal("1.05"))
                .execute();

        assertNotNull(adjust);
        assertEquals("00", adjust.getResponseCode());
    }

    @Test
    public void testAuthorizeKeyed() throws ApiException {
        Transaction response = card.authorize(new BigDecimal("11"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAddress(address)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getAvsResponseCode()); // verify an AVS response of some sort
    }

    @Test
    public void testAuthorizeSwiped() throws ApiException {
        Transaction response = track.authorize(new BigDecimal("100"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .withInvoiceNumber("1264")
                .withClientTransactionId("137149")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testAuthorizeToken() throws ApiException {
        Transaction response = tokenizedCard.authorize(new BigDecimal("19"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .withInvoiceNumber("1558")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testTokenizedCardSale() throws ApiException {
        String token = card.tokenize();
        assertNotNull(token);

        CreditCardData tokenizedCard = new CreditCardData();
        tokenizedCard.setToken(token);

        Transaction response = tokenizedCard.charge(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testSaleKeyed() throws ApiException {
        Transaction response = card.charge(new BigDecimal("100"))
                .withCurrency("USD")
                .withAllowDuplicates(false)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testSaleToken() throws ApiException {
        Transaction response = tokenizedCard.charge(new BigDecimal("1.29"))
                .withCurrency("USD")
                .withCashBack(new BigDecimal("0"))
                .withConvenienceAmt(new BigDecimal("0"))
                .withInvoiceNumber("1559")
                .withClientTransactionId("166909")
                .withAllowPartialAuth(false)
                .withAllowDuplicates(false)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testSettleBatch() throws ApiException {
        BatchSummary response = BatchService.closeBatch();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testVoid() throws ApiException {
        Transaction response = card.charge(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .withDescription("DEVICE_UNAVAILABLE")
                .execute();

        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void testRequestMUTOnSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal("12.34"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();

        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    public void testRefundByCard() throws ApiException {
        Transaction response = card.refund(new BigDecimal("15.11"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testRefundBySwipe() throws ApiException {
        Transaction response = track.refund(new BigDecimal("15.11"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void testSaleSwipedTrack1Pattern() throws ApiException {
        CreditTrackData track1 = new CreditTrackData();
        track1.setValue("%B5473500000000014^MC TEST CARD^251210199998888777766665555444433332");

        Transaction response = track1.charge(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // Exception tests

    @Test
    public void testAuthorizeWithoutAmount() throws ApiException {
        assertThrows(BuilderException.class, () -> {
            card.authorize(null)
                    .withCurrency("USD")
                    .execute();
        });
    }

    @Test
    public void testSaleWithoutAmount() throws ApiException {
        assertThrows(BuilderException.class, () -> {
            card.charge(null)
                    .withCurrency("USD")
                    .execute();
        });
    }

    @Test
    public void testRefundWithoutAmount() throws ApiException {
        assertThrows(BuilderException.class, () -> {
            card.refund(null)
                    .withCurrency("USD")
                    .execute();
        });
    }

    @Test
    public void testCredentialsError() throws ApiException {
        assertThrows(ConfigurationException.class, () -> {
            TransitConfig config = new TransitConfig();
            config.setAcceptorConfig(new AcceptorConfig());
            ServicesContainer.configureService(config);
        });
    }
}