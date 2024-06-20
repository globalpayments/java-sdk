package com.global.api.tests.terminals.upa;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.upa.Entities.Lodging;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaCreditTests {
    IDeviceInterface device;

    private static final String TERMINAL_REF_REQUIRED = "Terminal reference number is required";
    private static final String SUCCESS_STATUS = "Success";

    public UpaCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("10.253.146.155");
        config.setTimeout(45000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

//        config.setRequestLogger(new RequestFileLogger("creditTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(System.out::println);
        device.setOnMessageReceived(System.out::println);
    }

    @Test
    public void creditSaleSwipe() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("18.01"))
            .withGratuity(new BigDecimal("0.00"))
            .execute();

        runBasicTests(response);
    }

    @Test
    public void creditSaleChip() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.02"))
            .withRequestId(1202)
            .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.02"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleContactless() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.03"))
            .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.03"), response.getTransactionAmount());
    }
    @Test
    public void partialAmountSaleContactless() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("85.00"))
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("55.00"), response.getAuthorizeAmount());
    }
    @Test
    public void CardVerify() throws ApiException
    {
        // use Visa card
        TerminalResponse response = device.creditVerify()
            .withCardBrandStorage(StoredCredentialInitiator.Merchant)
            .withRequestMultiUseToken(true)
            .withClerkId(1234)
            .execute();

        runBasicTests(response);
        assertNotNull(response.getToken()); // will fail if MUTs aren't enabled
    }

    @Test
    public void BalanceInquiry() throws ApiException {
        TerminalResponse response = device.ebtBalance()
            .execute();

        runBasicTests(response);
        assertNotNull(response.getBalanceAmount());
    }

    @Test
    public void BlindRefund() throws ApiException
    {
        runBasicTests(
            device.creditRefund(new BigDecimal("1.23"))
                    .execute()
        );
    }

    @Test
    public void TipAdjust() throws ApiException
    {
        TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
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
    public void VoidTerminalTrans() throws ApiException
    {
        TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
            .withGratuity(new BigDecimal("0.00"))
            .execute();

        runBasicTests(response1);

        runBasicTests(
            device.creditVoid()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute()
        );
    }

    /**
     * Procedure: press the red 'X' button on the terminal when the terminal display prompts to present the card
     */
    @Test
    public void cancelledTrans() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.34"))
            .withGratuity(new BigDecimal("0.00"))
            .execute();

        assertNotNull(response);
        assertEquals("Failed", response.getStatus());
        assertEquals("APP001", response.getDeviceResponseCode());
        assertEquals("TRANSACTION CANCELLED BY USER", response.getDeviceResponseText());
    }

    @Test
    public void incrementalAuths() throws ApiException
    { // doesn't seem to work as described in 1.30 docs
        TerminalResponse response1 = device.creditAuth(new BigDecimal("10.00"))
                .execute();

        TerminalResponse response2 = device.creditAuth(new BigDecimal("5.00"))
                .withTransactionId(response1.getTransactionId())
                .execute();

        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test
    public void preAuths_And_IncrementalAuth() throws ApiException {
        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal(12.5));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse preAuthResponse = device.creditAuth(new BigDecimal("10.00"))
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

        TerminalResponse incrementResponse = device.creditAuth(new BigDecimal("10.00"))
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())

                .withLodging(lodging)
                .withTokenRequest(1)
                .withTokenValue("test")
                .withReferenceNumber(preAuthResponse.getTransactionId())
                .execute();

        assertNotNull(incrementResponse);
        assertEquals("00", incrementResponse.getDeviceResponseCode());

        String transactionId= preAuthResponse.getTransactionId();

        TerminalResponse captureResponse = device.creditCapture(new BigDecimal("20.00"))
                .withTerminalRefNumber(preAuthResponse.getTransactionId())
                .withTransactionId(transactionId)
                .execute();

        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getDeviceResponseCode());
    }

    @Test
    public void preAuths_capture() throws ApiException {
        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal(12.50));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse preAuthResponse = device.creditAuth(new BigDecimal("10.00"))
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

        TerminalResponse captureResponse = device.creditCapture(new BigDecimal("15"))
                .withReferenceNumber(preAuthResponse.getTransactionId())
                .execute();

        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());

    }

    @Test
    public void preAuthAndCapture() throws ApiException {
        TerminalResponse response = device.creditAuth(new BigDecimal("10.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
        assertEquals(new BigDecimal("10.00"), response.getTransactionAmount());

        TerminalResponse captureResponse = device.creditCapture(new BigDecimal("15"))
                .withTerminalRefNumber(response.getTransactionId())
                .withTransactionId(response.getTransactionId())
                .execute();

        assertNotNull(captureResponse);
        assertEquals(new BigDecimal("15.00"), captureResponse.getTransactionAmount());
    }
    @Test
    public void creditSaleSwipe_withoutHSAFSA() throws ApiException
    {
        AutoSubstantiation substantiation = new AutoSubstantiation();
        substantiation.setPrescriptionSubTotal(new BigDecimal(10));
        substantiation.setVisionSubTotal(new BigDecimal(10));
        TerminalResponse response = device.creditSale(new BigDecimal("12.01"))
                .withGratuity(new BigDecimal("0.00"))
                .withAutoSubstantiation(substantiation)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
    }

    @Test
    public void TipAdjust_AddReferenceNo() throws ApiException
    {
        TerminalResponse saleResponse = device.creditSale(new BigDecimal("10.50"))
                .execute();

        runBasicTests(saleResponse);

        TerminalResponse tipAdjustResponse = device.tipAdjust(new BigDecimal("1.50"))
                .withTerminalRefNumber(saleResponse.getTerminalRefNumber())
                .execute();

        runBasicTests(tipAdjustResponse);
        assertEquals(new BigDecimal("1.50"), tipAdjustResponse.getTipAmount());
        assertEquals(new BigDecimal("12.00"), tipAdjustResponse.getTransactionAmount());
    }

    @Test
    public void preAuthIncrementCompletion() throws ApiException {
        TerminalResponse preAuthResponse = device.creditAuth(new BigDecimal(100))
                .withCardBrandStorage(StoredCredentialInitiator.Merchant,"transId")
                .withClerkId(123)
                .execute();
        assertNotNull(preAuthResponse);
        assertEquals("00",preAuthResponse.getResponseCode());

        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal(12.50));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse incrementalAuthResponse = device.creditAuth(new BigDecimal(50))
                .withCardBrandStorage(StoredCredentialInitiator.Merchant,"transId")
                .withLodging(lodging)
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .withReferenceNumber(preAuthResponse.getTerminalRefNumber())
                .withClerkId(123)
                .execute();
        assertNotNull(incrementalAuthResponse);
        assertEquals("00",incrementalAuthResponse.getResponseCode());

        TerminalResponse completionResponse = device.creditCapture(new BigDecimal(145))
                .withTransactionId(preAuthResponse.getTransactionId())
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .withTerminalRefNumber(preAuthResponse.getTerminalRefNumber())
                .execute();
        assertNotNull(completionResponse);
        assertEquals("00",completionResponse);
    }
    @Test
    public void deletePreAuth_PreAuthAmount() throws ApiException {
        TerminalResponse response1 = device.creditAuth(new BigDecimal("10.00"))
                .execute();

        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        TerminalResponse response2 = device.deletePreAuth()
                .withTerminalRefNumber(response1.getTransactionId())
                .withPreAuthAmount(response1.getTransactionAmount())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }
    @Test
    public void deletePreAuth() throws ApiException {
        TerminalResponse response1 = device.creditAuth(new BigDecimal("10.00"))
                .execute();

        assertNotNull(response1);
        assertEquals("00", response1.getResponseCode());
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        TerminalResponse response2 = device.deletePreAuth()
                .withTerminalRefNumber(response1.getTransactionId())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }
    @Test // Without Terminal Reference Number
    public void deletePreAuth_Negative() throws ApiException {
            TerminalResponse response1 = device.creditAuth(new BigDecimal("10.00"))
                    .execute();

            assertNotNull(response1);
            assertEquals("00", response1.getResponseCode());
            assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

           IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,()->{
               device.deletePreAuth()
                    .withPreAuthAmount(new BigDecimal(12))
                    .execute();
        });
        assertEquals(TERMINAL_REF_REQUIRED,exception.getMessage());
    }

    /**
     * The purpose of this test is to confirm some new response properties are functioning
     *
     * Steps:
     * transaction sent from POS to T650P
     * T650P prompts for tip > tip option selected
     * confirm total amount, tip included
     * T650P prompts for card
     * B2 Interac test card ending 0003 inserted
     * select Chequing
     * enter PIN
     * payment approved
     *
     * @throws ApiException
     */
    @Test
    public void canadaSaleTest() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("18.01"))
                .withGratuity(new BigDecimal("0.00"))
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

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
}
