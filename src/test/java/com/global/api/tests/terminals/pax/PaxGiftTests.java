package com.global.api.tests.terminals.pax;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxGiftTests {
    private IDeviceInterface device;

    public PaxGiftTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.Pax_S300);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);

        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setDeviceConnectionConfig(deviceConfig);

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    //<editor-fold desc="GiftSale">
    @Test
    public void giftSale() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]01[FS]1000[FS][FS]1[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.giftSale(1, new BigDecimal(10))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftSaleManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]01[FS]1000[FS]5022440000000000098[FS]2[FS][FS][ETX]"));
            }
        });
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftSale(2, new BigDecimal(10))
                .withPaymentMethod(card)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftSaleWithInvoice() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]01[FS]1000[FS]5022440000000000098[FS]4[US]1234[FS][FS][ETX]"));
            }
        });

        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftSale(4, new BigDecimal(10))
                .withPaymentMethod(card)
                .withInvoiceNumber("1234")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltySaleManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T08[FS]1.35[FS]01[FS]1000[FS]5022440000000000098[FS]5[FS][FS][ETX]"));
            }
        });
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftSale(5, new BigDecimal(10))
                .withPaymentMethod(card)
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftSaleNoAmount() throws ApiException {
        device.giftSale(6).execute();
    }

    @Test(expected = BuilderException.class)
    public void giftSaleNoCurrency() throws ApiException {
        device.giftSale(7, new BigDecimal(10))
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftAddValue">
    @Test
    public void giftAddValueManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]10[FS]1000[FS]5022440000000000098[FS]8[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.giftAddValue(8)
                .withPaymentMethod(card)
                .withAmount(new BigDecimal(10))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftAddValue() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]10[FS]1000[FS][FS]9[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.giftAddValue(9)
                .withAmount(new BigDecimal(10))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltyAddValueManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T08[FS]1.35[FS]10[FS]1000[FS]5022440000000000098[FS]10[FS][FS][ETX]"));
            }
        });
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftAddValue(10)
                .withPaymentMethod(card)
                .withAmount(new BigDecimal(10))
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftAddValueNoAmount() throws ApiException {
        device.giftAddValue(11)
                .withAmount(null)
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void giftAddValueNoCurrency() throws ApiException {
        device.giftAddValue(12)
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftVoid">
    @Test
    public void giftVoidManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        final TerminalResponse saleResponse = device.giftSale(13, new BigDecimal(10)).withPaymentMethod(card).execute();

        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]16[FS][FS][FS]14[FS][FS]HREF="+saleResponse.getTransactionId()+"[ETX]"));
            }
        });

        TerminalResponse voidResponse = device.giftVoid(14)
                .withTransactionId(saleResponse.getTransactionId())
                .execute();

        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftVoidNoCurrency() throws ApiException {
        device.giftVoid(15)
                .withCurrency(null)
                .withTransactionId("1")
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void giftVoidNoTransactionId() throws ApiException {
        device.giftVoid(16).withTransactionId(null).execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftBalance">
    @Test
    public void giftBalance() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]23[FS][FS][FS]17[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.giftBalance(17)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftBalanceManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T06[FS]1.35[FS]23[FS][FS]5022440000000000098[FS]18[FS][FS][ETX]"));
            }
        });
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftBalance(18)
                .withPaymentMethod(card)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltyBalanceManual() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T08[FS]1.35[FS]23[FS][FS]5022440000000000098[FS]19[FS][FS][ETX]"));
            }
        });
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.giftBalance(19)
                .withPaymentMethod(card)
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftBalanceNoCurrency() throws ApiException {
        device.giftBalance(20)
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="Certification">
    @Test
    public void test_case_15a() throws ApiException {
        TerminalResponse response = device.giftBalance(1).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getBalanceAmount());
    }

    @Test
    public void test_case_15b() throws ApiException {
        TerminalResponse response = device.giftAddValue(2).withAmount(new BigDecimal("8")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_case_15c() throws ApiException {
        TerminalResponse response = device.giftSale(3, new BigDecimal("1")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    //</editor-fold>
}
