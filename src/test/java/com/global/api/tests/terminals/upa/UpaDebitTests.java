package com.global.api.tests.terminals.upa;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.upa.Entities.Lodging;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static com.global.api.tests.gpapi.BaseGpApiTest.generateRandomBigDecimalFromRange;
import static org.junit.Assert.*;

public class UpaDebitTests {
    IDeviceInterface device;
    private static final String SUCCESS_STATUS = "Success";
    private final BigDecimal amount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("10"), 2);

    public UpaDebitTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.116");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("Success", response.getDeviceResponseText());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
    @Test
    public void debitSaleSwipe() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        runBasicTests(response);
    }

    @Test
    public void debitSaleSwipe_WithTip() throws ApiException {
        BigDecimal tipAmount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("2"), 2);

        TerminalResponse response = device.sale(amount)
                .withGratuity(tipAmount)
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        runBasicTests(response);
        assertEquals(amount.add(tipAmount), response.getTransactionAmount());
        assertEquals(tipAmount, response.getTipAmount());
    }

    @Test
    public void debitSaleChip() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("12.02"))
                .withRequestId(1202)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.02"), response.getTransactionAmount());
    }

    @Test
    public void debitSaleAmountFormat() throws ApiException {
        //Transaction amount tested could be 0.X, 0.XX, XX.XXX etc
        TerminalResponse response = device.sale(new BigDecimal("5.7983"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("5.80"), response.getTransactionAmount());
    }

    @Test
    public void debitSaleWithInvoiceNbr() throws ApiException {
        //Test withInvoiceNumber since no sale has an invoice number.
        TerminalResponse response = device.sale(new BigDecimal("1.10"))
                .withInvoiceNumber("123")
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("1.10"), response.getTransactionAmount());
    }

    @Test
    public void debitSaleContactless() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("12.03"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.03"), response.getTransactionAmount());
    }

    @Test
    public void debitSaleSwipe_withoutHSAFSA() throws ApiException {
        AutoSubstantiation substantiation = new AutoSubstantiation();
        substantiation.setPrescriptionSubTotal(new BigDecimal(10));
        substantiation.setVisionSubTotal(new BigDecimal(10));
        TerminalResponse response = device.sale(new BigDecimal("12.01"))
                .withGratuity(new BigDecimal("0"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withAutoSubstantiation(substantiation)
                .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
    }

    @Test
    public void voidDebitWithReferenceNoOfSale() throws ApiException {
        TerminalResponse response1 = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();
        runBasicTests(response1);
        TerminalResponse response = device.voidTransaction()
                .withTransactionId(response1.getTransactionId())
                .execute();
        runBasicTests(response);
    }

    @Test
    public void debitRefund_Linked() throws ApiException, InterruptedException {
        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal(0))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        assertNotNull(response);

        Thread.sleep(15000);

        TerminalResponse refundResponse = device.refund(amount)
                .withTransactionId(response.getTransactionId())
                .execute();

        assertNotNull(refundResponse);
    }
    @Test
    public void debit_refundByRefNo_withTip() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10.00"))
                .withGratuity(new BigDecimal(2))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();
        runBasicTests(response);

        TerminalResponse refundResponse = device.refund(response.getBaseAmount())
                .withGratuity(response.getTipAmount())
                .withTransactionId(response.getTransactionId())
                .execute();
        runBasicTests(refundResponse);
    }

    @Test
    public void debitReverse() throws ApiException {
        TerminalResponse response1 = device.sale(amount)
                .withGratuity(new BigDecimal("0"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();
        runBasicTests(response1);

        TerminalResponse response2 = device.reverse()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute();
        runBasicTests(response2);
    }

    @Test
    public void TipAdjust_withTerminalRefNumber() throws ApiException {
        TerminalResponse response1 = device.sale(new BigDecimal("1.34"))
                .withPaymentMethodType(PaymentMethodType.Debit)
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
                .withPaymentMethodType(PaymentMethodType.Debit)
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
    public void incrementalAuths() throws ApiException {
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        TerminalResponse response2 = device.authorize(new BigDecimal("5.00"))
                .withTransactionId(response1.getTransactionId())
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test
    public void preAuths_capture() throws ApiException {
        Lodging lodging = new Lodging();
        lodging.setCheckInDate(DateTime.now().toString("MMddyyyy"));
        lodging.setDailyRate(new BigDecimal("12.50"));
        lodging.setFolioNumber(10);
        lodging.setStayDuration(30);

        TerminalResponse preAuthResponse = device.authorize(new BigDecimal("10.00"))
                .withPaymentMethodType(PaymentMethodType.Debit)
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
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withTransactionId(preAuthResponse.getTransactionId())
                .withPreAuthAmount(preAuthResponse.getTransactionAmount())
                .execute();

        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void deletePreAuth() throws ApiException {
        TerminalResponse response1 = device.authorize(new BigDecimal("10.00"))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();

        assertNotNull(response1);
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));

        TerminalResponse response2 = device.deletePreAuth()
                .withTransactionId(response1.getTransactionId())
                .execute();
        assertNotNull(response2);
        assertTrue(response1.getStatus().equalsIgnoreCase(SUCCESS_STATUS));
    }
    @Test
    public void CardVerify() throws ApiException {
        // use Visa card
        TerminalResponse response = device.verify()
                .withCardBrandStorage(StoredCredentialInitiator.Merchant)
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withRequestMultiUseToken(true)
                .withClerkId(1234)
                .execute();

        runBasicTests(response);
        assertNotNull(response.getToken()); // will fail if MUTs aren't enabled
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

}
