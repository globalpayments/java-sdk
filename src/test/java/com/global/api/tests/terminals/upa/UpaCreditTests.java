package com.global.api.tests.terminals.upa;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.GatewayDuplicateException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.upa.Entities.Lodging;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.tests.gpapi.BaseGpApiTest.generateRandomBigDecimalFromRange;
import static org.junit.Assert.*;

public class UpaCreditTests {
    IDeviceInterface device;
    private static final String REFERENCE_REQUIRED = "Reference number is required";
    private static final String SUCCESS_STATUS = "Success";
    private final BigDecimal amount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("10"), 2);

    public UpaCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.144.111");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    @Test
    public void creditSaleSwipe() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .execute();

        runBasicTests(response);
    }

    @Test
    public void creditSaleWithAllowDuplicate() throws ApiException {
        BigDecimal fixedAmount = new BigDecimal("5.50");
        TerminalResponse response1 = device.sale(fixedAmount)
                .withGratuity(new BigDecimal("0"))
                .execute();

        runBasicTests(response1);
        //run a second time and verify that duplicate error is raised
        try {
            TerminalResponse response2 = device.sale(fixedAmount)
                    .withGratuity(new BigDecimal("0"))
                    .execute();
        } catch (GatewayDuplicateException ex) {
            assertEquals(ex.getAdditionalDuplicateData().getOriginalGatewayTxnId(), response1.getTransactionId());
        }
        //run a third time with allow duplicates flag an ensure that it runs.
        TerminalResponse response3 = device.sale(fixedAmount)
                .withGratuity(new BigDecimal("0"))
                .withAllowDuplicates(true)
                .execute();

        runBasicTests(response3);
    }

    @Test
    public void creditSaleSwipe_WithTip() throws ApiException {
        BigDecimal tipAmount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("2"), 2);

        TerminalResponse response = device.sale(amount)
                .withGratuity(tipAmount)
                .execute();

        runBasicTests(response);
        assertEquals(amount.add(tipAmount), response.getTransactionAmount());
        assertEquals(tipAmount, response.getTipAmount());
    }

    @Test
    public void creditSaleChip() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("12.02"))
                .withRequestId(1202)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.02"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleAmountFormat() throws ApiException {
        //Transaction amount tested could be 0.X, 0.XX, XX.XXX etc
        TerminalResponse response = device.sale(new BigDecimal("5.7983"))
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("5.80"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleWithInvoiceNbr() throws ApiException {
        //Test withInvoiceNumber since no sale has an invoice number.
        TerminalResponse response = device.sale(new BigDecimal("1.10"))
                .withInvoiceNumber("123")
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("1.10"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleContactless() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("12.03"))
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.03"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleSwipe_withoutHSAFSA() throws ApiException {
        AutoSubstantiation substantiation = new AutoSubstantiation();
        substantiation.setPrescriptionSubTotal(new BigDecimal(10));
        substantiation.setVisionSubTotal(new BigDecimal(10));
        TerminalResponse response = device.sale(new BigDecimal("12.01"))
                .withGratuity(new BigDecimal("0"))
                .withAutoSubstantiation(substantiation)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
    }

    @Test
    public void partialAmountSaleContactless() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("85.00"))
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("55.00"), response.getAuthorizeAmount());
    }

    @Test
    public void CardVerify() throws ApiException {
        // use Visa card
        TerminalResponse response = device.verify()
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .withRequestMultiUseToken(true)
                .withClerkId(1234)
                .execute();

        runBasicTests(response);
        assertNotNull(response.getToken()); // will fail if MUTs aren't enabled
    }

    @Test
    public void BlindRefund() throws ApiException {
        runBasicTests(
                device.refund(new BigDecimal("1.23"))
                        .execute()
        );
    }

    @Test
    public void TipAdjust_withTerminalRefNumber() throws ApiException {
        TerminalResponse response1 = device.sale(new BigDecimal("12.34"))
                .execute();

        runBasicTests(response1);

        TerminalResponse response2 = device.tipAdjust(new BigDecimal("1.50"))
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute();

        runBasicTests(response2);
        assertEquals(new BigDecimal("1.50"), response2.getTipAmount());
        assertEquals(new BigDecimal("13.84"), response2.getTransactionAmount());
    }

    @Test
    public void TipAdjust_withReferenceNo() throws ApiException {
        TerminalResponse saleResponse = device.sale(new BigDecimal("10.50"))
                .execute();

        runBasicTests(saleResponse);

        TerminalResponse tipAdjustResponse = device.tipAdjust(new BigDecimal("1.50"))
                .withTransactionId(saleResponse.getTransactionId())
                .execute();

        runBasicTests(tipAdjustResponse);
        assertEquals(new BigDecimal("1.50"), tipAdjustResponse.getTipAmount());
        assertEquals(new BigDecimal("12.00"), tipAdjustResponse.getTransactionAmount());
    }

    @Test
    public void VoidTerminalTrans() throws ApiException, InterruptedException {
        TerminalResponse response1 = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .execute();

        runBasicTests(response1);

        Thread.sleep(5_000);

        runBasicTests(
                device.voidTransaction()
                        .withTransactionId(response1.getTransactionId())
                        .execute()
        );
    }

    @Test
    public void Void_UsingTranNo() throws ApiException, InterruptedException {
        TerminalResponse response1 = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .execute();

        runBasicTests(response1);

        TerminalResponse response2 = device.voidTransaction()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute();
        runBasicTests(response2);
    }

    @Test
    public void creditReverse() throws ApiException, InterruptedException {
        TerminalResponse response1 = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .execute();
        runBasicTests(response1);

        TerminalResponse response2 = device.reverse()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute();
        runBasicTests(response2);
    }

    /**
     * Procedure: press the red 'X' button on the terminal when the terminal display prompts to present the card
     */
    @Test
    public void cancelledTrans() throws ApiException {
        GatewayException exception = assertThrows(GatewayException.class, () -> device.sale(new BigDecimal("12.34"))
                .withGratuity(new BigDecimal("0"))
                .execute());
        assertEquals("Unexpected Device Response :APP001 - TRANSACTION CANCELLED BY USER", exception.getMessage());
    }

    @Test
    public void incrementalAuths() throws ApiException { // doesn't seem to work as described in 1.30 docs
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .execute();

        TerminalResponse response2 = device.authorize(new BigDecimal("5.00"))
                .withTransactionId(response1.getTransactionId())
                .execute();

        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test
    public void preAuths_And_IncrementalAuth() throws ApiException {
        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal("12.5"));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse preAuthResponse = device.authorize(new BigDecimal("10.00"))
                .withInvoiceNumber("12345")
                .withLodging(lodging)
                .withTokenRequest(1)
                .withTokenValue("test")
                .withDirectMarketInvoiceNumber("12345")
                .withDirectMarketShipDay(12)
                .withDirectMarketShipMonth(10)
                .execute();

        assertNotNull(preAuthResponse);
        assertEquals("00", preAuthResponse.getDeviceResponseCode());
        assertTrue(preAuthResponse.getStatus().equalsIgnoreCase("Success"));

        TerminalResponse incrementResponse = device.authorize(new BigDecimal("10.00"))
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())

                .withLodging(lodging)
                .withTokenRequest(1)
                .withTokenValue("test")
                .withReferenceNumber(preAuthResponse.getTransactionId())
                .execute();

        assertNotNull(incrementResponse);
        assertEquals("00", incrementResponse.getDeviceResponseCode());

        String transactionId = preAuthResponse.getTransactionId();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TerminalResponse captureResponse = device.capture(new BigDecimal("20.00"))
                .withTransactionId(transactionId)
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .execute();

        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getDeviceResponseCode());
    }

    @Test
    public void preAuths_capture() throws ApiException {
        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal("12.50"));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse preAuthResponse = device.authorize(new BigDecimal("10.00"))
                .withInvoiceNumber("12345")
                .withLodging(lodging)
                .withTokenRequest(1)
                .withTokenValue("test")
                .withDirectMarketInvoiceNumber("12345")
                .withDirectMarketShipDay(12)
                .withDirectMarketShipMonth(10)
                .execute();

        assertNotNull(preAuthResponse);
        assertEquals("00", preAuthResponse.getDeviceResponseCode());
        assertTrue(preAuthResponse.getStatus().equalsIgnoreCase("Success"));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TerminalResponse captureResponse = device.capture(new BigDecimal("15.00"))
                .withTransactionId(preAuthResponse.getTransactionId())
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .execute();

        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void preAuthAndCapture() throws ApiException {
        TerminalResponse response = device.authorize(new BigDecimal("10.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
        assertEquals(new BigDecimal("10.00"), response.getTransactionAmount());

        TerminalResponse captureResponse = device.capture(new BigDecimal("10.00"))
                .withTransactionId(response.getTransactionId())
                .withPreAuthAmount(response.getTransactionAmount())
                .execute();

        assertNotNull(captureResponse);
        assertEquals(new BigDecimal("10.00"), captureResponse.getTransactionAmount());
    }

    @Test
    public void preAuthIncrementCompletion() throws ApiException {
        TerminalResponse preAuthResponse = device.authorize(new BigDecimal("10.00"))
                .withClerkId(123)
                .execute();
        assertNotNull(preAuthResponse);
        assertEquals("00", preAuthResponse.getResponseCode());

        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal("12.50"));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse incrementalAuthResponse = device.authorize(new BigDecimal("5.00"))
                .withLodging(lodging)
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .withTransactionId(preAuthResponse.getTransactionId())
                .withClerkId(123)
                .execute();
        assertNotNull(incrementalAuthResponse);
        assertEquals("00", incrementalAuthResponse.getResponseCode());


        TerminalResponse completionResponse = device.capture(new BigDecimal("15.00"))
                .withTransactionId(preAuthResponse.getTransactionId())
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .execute();
        assertNotNull(completionResponse);
        assertEquals("00", completionResponse.getResponseCode());
    }

    @Test
    public void deletePreAuth_PreAuthAmount() throws ApiException {
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .execute();

        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        TerminalResponse response2 = device.deletePreAuth()
                .withTransactionId(response1.getTransactionId())
                .withPreAuthAmount(response1.getTransactionAmount())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test
    public void deletePreAuth() throws ApiException {
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .execute();

        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        TerminalResponse response2 = device.deletePreAuth()
                .withTransactionId(response1.getTransactionId())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test // Without Reference Number
    public void deletePreAuth_Negative() throws ApiException {
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .execute();

        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> device.deletePreAuth()
                .withPreAuthAmount(new BigDecimal(12))
                .execute());
        assertEquals(REFERENCE_REQUIRED, exception.getMessage());
    }

    /**
     * The purpose of this test is to confirm some new response properties are functioning
     * <p>
     * Steps:
     * transaction sent from POS to T650P
     * prompts for tip > tip option selected
     * confirm total amount, tip included
     * T650P prompts for card
     * B2 Interac test card ending 0003 inserted
     * select Chequing
     * enter PIN
     * payment approved
     */
    @Test
    public void canadaSaleTest() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("18.01"))
                .withGratuity(new BigDecimal("0"))
                .execute();

        runBasicTests(response);
        assertNotNull(response.getPinVerified());
        assertNotNull(response.getAccountType());
        assertNotNull(response.getIssuerResponseCode());
        assertNotNull(response.getIsoResponseCode());
        assertNotNull(response.getBankResponseCode());
        assertNotNull(response.getApplicationName());
        assertNotNull(response.getCardHolderLanguage());
    }

    @Test
    public void CreditRefund_Linked() throws ApiException, InterruptedException {
        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal(0))
                .execute();

        assertNotNull(response);


        Thread.sleep(15000);

        TerminalResponse refundResponse = device.refund(amount)
                .withTransactionId(response.getTransactionId())
                .execute();

        assertNotNull(refundResponse);
    }

    @Test
    public void voidDebitMerchantIdMappingWithHardcodedValue() throws ApiException {

        TerminalResponse response = device.voidTransaction().withTerminalRefNumber("1234").execute();
        assertNotNull(response.getMerchantId());
    }

    @Test
    public void voidDebitMerchantIdMappingWithReferenceNoOfSale() throws ApiException {
        TerminalResponse response1 = device.sale(amount).withGratuity(new BigDecimal("0")).execute();
        runBasicTests(response1);
        TerminalResponse response = device.voidTransaction().withTransactionId(response1.getTransactionId()).execute();
        runBasicTests(response);
        assertNotNull(response.getMerchantId());
    }

    @Test
    public void refundByRefNo_withTip() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10.00"))
                .withGratuity(new BigDecimal(2))
                .execute();
        runBasicTests(response);

        TerminalResponse refundResponse = device.refund(response.getBaseAmount())
                .withGratuity(response.getTipAmount())
                .withTransactionId(response.getTransactionId())
                .execute();
        runBasicTests(refundResponse);
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getDeviceResponseText());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
}
