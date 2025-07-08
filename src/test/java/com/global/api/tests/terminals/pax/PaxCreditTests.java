package com.global.api.tests.terminals.pax;

import com.global.api.entities.Address;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Ignore;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static com.global.api.tests.gpapi.BaseGpApiTest.generateRandomBigDecimalFromRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxCreditTests {
    private final IDeviceInterface device;
    private final BigDecimal amount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("10"), 2);

    public PaxCreditTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.51.252");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);

//        device.setOnMessageSent(System.out::println);
//        device.setOnMessageReceived(message -> {
//            System.out.println(new String(message));
//        });
    }

    @Test
    public void creditSale() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withAllowDuplicates(true)
                .withGratuity(new BigDecimal("1.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleManual() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.sale(amount)
                .withAllowDuplicates(true)
                .withPaymentMethod(card)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithMerchantFee() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertNotNull(response.getMerchantFee());
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithSignatureCapture() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2026);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.sale(amount)
                .withAllowDuplicates(true)
                .withPaymentMethod(card)
                .withAddress(address)
                .withSignatureCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditSaleNoAmount() throws ApiException {
        device.sale(null).execute();
    }

    @Test
    public void creditAuthCapture() throws ApiException {
        TerminalResponse response = device.authorize(amount)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse captureResponse = device.capture(response.getTransactionAmount())
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void creditAuthCaptureManual() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.authorize(amount)
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse captureResponse = device.capture(response.getTransactionAmount())
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoAmount() throws ApiException {
        device.authorize(null)
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void creditCaptureNoTransactionId() throws ApiException {
        device.capture().execute();
    }

    @Test
    public void creditRefundByTransactionId() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse saleResponse = device.sale(amount)
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse returnResponse = device.refund(saleResponse.getTransactionAmount())
                .withTransactionId(saleResponse.getTransactionId())
                .withAuthCode(saleResponse.getAuthorizationCode())
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test
    public void creditRefundByCard() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        TerminalResponse returnResponse = device.refund(amount)
                .withPaymentMethod(card)
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test @Ignore
    public void creditRefundByToken() throws ApiException {
        // TODO: Needs new token value
        String token = "GLl8b708JHBbLdMfHf6H4460";
        TerminalResponse returnResponse = device.refund(amount)
                .withToken(token)
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditRefundNoAmount() throws ApiException {
        device.refund().execute();
    }

    @Test
    public void creditVerify() throws ApiException {
        TerminalResponse response = device.verify().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVerifyManual() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.verify()
                .withPaymentMethod(card)
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void tokenize() throws ApiException {
        TerminalResponse response = device.verify()
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getToken());
    }

    @Test
    public void creditVoid() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse saleResponse = device.sale(amount)
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse returnResponse = device.voidTransaction()
                .withTransactionId(saleResponse.getTransactionId())
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditVoidNoTransactionId() throws ApiException {
        device.voidTransaction().execute();
    }

    @Test
    public void creditSale_with_Gratuity() throws ApiException {
        BigDecimal amt = amount;
        TerminalResponse response = device.sale(amt)
                .withAllowDuplicates(true)
                .withGratuity(new BigDecimal("1.50"))
                .execute();
        assertNotNull(response);
        assertEquals(new BigDecimal(amt + "1.50"), response.getTransactionAmount());
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSale_TipAdjust() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("13.50"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse tipResponse = device.tipAdjust(new BigDecimal("11.50"))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(tipResponse);
        assertEquals("00", tipResponse.getResponseCode());
    }

    @Test @Ignore
    public void creditAuth_With_TransactionIdentifier() throws ApiException {
        TerminalResponse preResponse = device.authorize(amount)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(preResponse);
        assertEquals("00", preResponse.getResponseCode());

        TerminalResponse response = device.authorize(amount)
                .withAllowDuplicates(true)
                .withToken(preResponse.getToken())
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder, preResponse.getCardBrandTransactionId())
                // TODO: missing something from the request
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test @Ignore
    public void creditSale_WithCardBrandInfo() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .withGratuity(BigDecimal.ZERO)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getCardBrandTransactionId());
        assertNotNull(response.getToken());

        TerminalResponse mutSaleResponse = device.sale(amount)
                .withToken(response.getToken())
                .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                .withAllowDuplicates(true)
                .withGratuity(BigDecimal.ZERO)
                // TODO: missing something from the request
                .execute();
        assertNotNull(mutSaleResponse);
        assertEquals("00", mutSaleResponse.getResponseCode());
    }

    /**
     * NOTE: This test does not function with PAX S300
     */
    @Test
    public void creditSaleWithAllowPartialAuth() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(155))
                .withAllowPartialAuth(true)
                .withAllowDuplicates(true)
                .execute();

        assertNotNull(response);

        assertEquals("10", response.getResponseCode());
        assertEquals("100", response.getAmountAuthorized());
        assertEquals(new BigDecimal("100"), response.getTransactionAmount());
        assertEquals(new BigDecimal("55"), response.getAmountDue());
        assertEquals(new BigDecimal(100), response.getAuthorizeAmount());
    }

    /**
     *
     * This test should demonstrate that the device IS prompting for a tip
     * when a gratuity amount isn't provided to the builder. This assumes that
     * the device has been configured for gratuity, which is something that is
     * set at the terminal file level.
     *   **Requires end-user confirmation**
     */
    @Test
    public void testTipPrompt() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * This test should demonstrate that the device is NOT prompting for a
     * tip when a gratuity amount IS provided to the builder. This assumes that
     * the device is configured for gratuity which is something that is set at
     * the terminal file level.
     *   **Requires end-user confirmation**
     */
    @Test
    public void testTipNoPrompt() throws ApiException {
        TerminalResponse response = device.sale(amount)
                .withGratuity(new BigDecimal("3.00")) // this makes for an $18.34 sale on device
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    public void logStuff() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String newLine = System.lineSeparator();

        device.setOnMessageSent(message -> {
            assertNotNull(message);

            FileWriter writer;
            try {
                writer = new FileWriter("logs.txt", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PrintWriter printer = new PrintWriter(writer);
            printer.append(timeStamp).append(newLine);
            printer.println("Sent to device:");
            printer.println(message + newLine);
            printer.close();
        });

        device.setOnMessageReceived(message -> {
            assertNotNull(message);

            FileWriter writer;
            try {
                writer = new FileWriter("logs.txt", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PrintWriter printer = new PrintWriter(writer);
            printer.println("Received from device:");
            printer.println(Arrays.toString(message) + newLine + newLine);
            printer.close();
        });
    }
}
