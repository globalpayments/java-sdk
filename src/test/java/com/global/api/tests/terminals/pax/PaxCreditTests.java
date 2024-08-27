package com.global.api.tests.terminals.pax;

import com.global.api.entities.Address;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxCreditTests {
    private IDeviceInterface device;
    private String rec_message;

    public PaxCreditTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.0.5");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void creditSale() throws ApiException {
        device.setOnMessageSent(message -> {
            assertNotNull(message);
        });

        TerminalResponse response = device.creditSale(new BigDecimal(20.99))
                .withAllowDuplicates(true)
                .withGratuity(new BigDecimal("1.00"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1100[FS]4005554444444460[US]1217[US]123[US][US][US]1[FS]1[FS]95124[US]1 Heartland Way[FS][FS][FS][FS][ETX]"));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.creditSale(new BigDecimal(11))
                .withAllowDuplicates(true)
                .withPaymentMethod(card)
                .withAddress(address)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithMerchantFee() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                }
        });

        TerminalResponse response = device.creditSale(new BigDecimal(10))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertNotNull(response.getMerchantFee());
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithSignatureCapture() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1200[FS]4005554444444460[US]1217[US]123[US][US][US]1[FS]1[FS]95124[US]1 Heartland Way[FS][FS][FS][FS]SIGN=1[ETX]"));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(17);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.creditSale(new BigDecimal(12))
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
        device.creditSale().execute();
    }

    @Test
    public void creditAuthCapture() throws ApiException {
        rec_message = "[STX]T00[FS]1.35[FS]03[FS]1200[FS][US][US][US][US][US]1[FS]1[FS][FS][FS][FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.creditAuth(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        rec_message = String.format("[STX]T00[FS]1.35[FS]04[FS]1200[FS][FS]2[FS][FS][FS][FS][FS]HREF=%s[ETX]", response.getTransactionId());
        TerminalResponse captureResponse = device.creditCapture(new BigDecimal("12"))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test
    public void creditAuthCaptureManual() throws ApiException {
        rec_message = "";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.creditAuth(new BigDecimal("12"))
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse captureResponse = device.creditCapture(new BigDecimal("12"))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(captureResponse);
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoAmount() throws ApiException {
        device.creditAuth().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditCaptureNoTransactionId() throws ApiException {
        device.creditCapture().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoAuthCode() throws ApiException {
        device.creditAuth(new BigDecimal(13))
                .withTransactionId("1234567")
                .execute();
    }

    @Test
    public void creditRefundByTransactionId() throws ApiException {
        rec_message = "[STX]T00[FS]1.35[FS]01[FS]1600[FS]4005554444444460[US]1217[US]123[US][US][US]1[FS]1[FS]95124[US]1 Heartland Way[FS][FS][FS][FS]TOKENREQUEST=1[ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse saleResponse = device.creditSale(new BigDecimal(16))
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        rec_message = String.format("[STX]T00[FS]1.35[FS]02[FS]1600[FS][FS]2[US][US]%s[FS][FS][FS][FS][FS]HREF=%s[ETX]", saleResponse.getAuthorizationCode(), saleResponse.getTransactionId());
        TerminalResponse returnResponse = device.creditRefund(new BigDecimal(16))
                .withTransactionId(saleResponse.getTransactionId())
                .withAuthCode(saleResponse.getAuthorizationCode())
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test
    public void creditRefundByCard() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]02[FS]1400[FS]4005554444444460[US]1217[FS]2[FS][FS][FS][FS][FS][ETX]"));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        TerminalResponse returnResponse = device.creditRefund(new BigDecimal(14))
                .withPaymentMethod(card)
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test
    public void creditRefundByToken() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]02[FS]1400[FS][FS]2[FS][FS][FS][FS][FS]TOKEN=GLl8b708JHBbLdMfHf6H4460[ETX]"));
            }
        });

        String token = "GLl8b708JHBbLdMfHf6H4460";
        TerminalResponse returnResponse = device.creditRefund(new BigDecimal(14))
                .withToken(token)
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditRefundNoAmount() throws ApiException {
        device.creditRefund().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditRefundByTransactionIdNoAuthCode() throws ApiException {
        device.creditRefund(new BigDecimal(13))
                .withTransactionId("1234567")
                .execute();
    }

    @Test
    public void creditVerify() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]24[FS][FS][FS]1[FS][FS][FS][FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditVerify().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVerifyManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]24[FS][FS]4005554444444460[US]1217[FS]1[FS]95124[US]1 Heartland Way[FS][FS][FS][FS][ETX]"));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(25);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse response = device.creditVerify()
                .withPaymentMethod(card)
                .withAddress(address)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void tokenize() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]32[FS][FS][FS]1[FS][FS][FS][FS][FS]TOKENREQUEST=1[ETX]"));
            }
        });

        TerminalResponse response = device.creditVerify()
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getToken());
    }

    @Test
    public void creditVoid() throws ApiException {
        rec_message = "[STX]T00[FS]1.35[FS]01[FS]1600[FS]4005554444444460[US]1217[US]123[US][US][US]1[FS]1[FS]95124[US]1 Heartland Way[FS][FS][FS][FS]TOKENREQUEST=1[ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                //assertTrue(message.startsWith(rec_message));
            }
        });

        CreditCardData card = new CreditCardData();
        card.setNumber("4005554444444460");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        Address address = new Address();
        address.setStreetAddress1("1 Heartland Way");
        address.setPostalCode("95124");

        TerminalResponse saleResponse = device.creditSale(new BigDecimal(16))
                .withPaymentMethod(card)
                .withAddress(address)
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());

        TerminalResponse returnResponse = device.creditVoid()
                .withTransactionId(saleResponse.getTransactionId())
                .execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditVoidNoTransactionId() throws ApiException {
        device.creditVoid().execute();
    }

    @Test
    public void creditSale_with_Gratuity() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
            }
        });

        TerminalResponse response = device.creditSale(new BigDecimal(13.50))
                .withAllowDuplicates(true)
                .withGratuity(new BigDecimal(1.50))
                .execute();
        assertNotNull(response);
        assertEquals(new BigDecimal("15"),response.getTransactionAmount());
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSale_TipAdjust() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
            }
        });

        TerminalResponse response = device.creditSale(new BigDecimal(13.50))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse tipResponse = device.tipAdjust(new BigDecimal(11.50))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(tipResponse);
        assertEquals("00", tipResponse.getResponseCode());
    }

    @Test
    public void creditAuth_With_TransactionIdentifier() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
            }
        });

        TerminalResponse preResponse = device.creditAuth(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(preResponse);
        assertEquals("00", preResponse.getResponseCode());

        TerminalResponse response = device.creditAuth(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .withToken(preResponse.getToken())
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder,preResponse.getCardBrandTransactionId())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVerify_With_TransactionIdentifier() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
            }
        });
        TerminalResponse preResponse = device.creditVerify()
                .withRequestMultiUseToken(true)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(preResponse);
        assertNotNull(preResponse.getCardBrandTransactionId());
        assertEquals("00", preResponse.getResponseCode());

        TerminalResponse response = device.creditVerify()
                .withToken(preResponse.getToken())
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.CardHolder,preResponse.getCardBrandTransactionId())
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void creditSaleWithCardBrandInfo() throws ApiException {
        TerminalResponse response = device.creditSale(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .withRequestMultiUseToken(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getCardBrandTransactionId());
        assertNotNull(response.getToken());

        TerminalResponse mutSaleResponse = device.creditSale(new BigDecimal("12"))
                .withToken(response.getToken())
                .withAllowDuplicates(true)
                .withCardBrandStorage(StoredCredentialInitiator.Merchant, response.getCardBrandTransactionId())
                .execute();
        assertNotNull(mutSaleResponse);
        assertEquals("00", mutSaleResponse.getResponseCode());

    }

    /**
     * NOTE: This test does not function with PAX S300
     * @throws ApiException
     */
    @Test
    public void creditSaleWithAllowPartialAuth() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);

            }
        });
        TerminalResponse response = device.creditSale(new BigDecimal(155))
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
     *
     *   **Requires end-user confirmation**
     *
     * @throws ApiException
     */
    @Test
    public void testTipPrompt() throws ApiException {
        TerminalResponse response = device.creditSale(new BigDecimal(12.34))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    /**
     * This test should demonstrate that the device is NOT prompting for a
     * tip when a gratuity amount IS provided to the builder. This assumes that
     * the device is configured for gratuity which is something that is set at
     * the terminal file level.
     *
     *   **Requires end-user confirmation**
     *
     * @throws ApiException
     */
    @Test
    public void testTipNoPrompt() throws ApiException {
        TerminalResponse response = device.creditSale(new BigDecimal(15.34))
                .withGratuity(new BigDecimal(3.00)) // this makes for an $18.34 sale on device
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
