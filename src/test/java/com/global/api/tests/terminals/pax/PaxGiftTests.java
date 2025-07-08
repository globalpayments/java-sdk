package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxGiftTests {
    private final IDeviceInterface device;

    public PaxGiftTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    //<editor-fold desc="GiftSale">
    @Test
    public void giftSale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftSaleManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftSaleWithInvoice() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .withInvoiceNumber("1234")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltySaleManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftSaleNoAmount() throws ApiException {
        device.sale(null)
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void giftSaleNoCurrency() throws ApiException {
        device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftAddValue">
    @Test
    public void giftAddValueManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.addValue()
                .withPaymentMethod(card)
                .withAmount(new BigDecimal(10))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftAddValue() throws ApiException {
        TerminalResponse response = device.addValue()
                .withAmount(new BigDecimal(10))
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltyAddValueManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.addValue()
                .withPaymentMethod(card)
                .withAmount(new BigDecimal(10))
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftAddValueNoAmount() throws ApiException {
        device.addValue()
                .withAmount(null)
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void giftAddValueNoCurrency() throws ApiException {
        device.addValue()
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftVoid">
    @Test
    public void giftVoidManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        final TerminalResponse saleResponse = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .execute();

        TerminalResponse voidResponse = device.voidTransaction()
                .withTransactionId(saleResponse.getTransactionId())
                .execute();

        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftVoidNoCurrency() throws ApiException {
        device.voidTransaction()
                .withCurrency(null)
                .withTransactionId("1")
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void giftVoidNoTransactionId() throws ApiException {
        device.voidTransaction().withTransactionId(null).execute();
    }
    //</editor-fold>

    //<editor-fold desc="GiftBalance">
    @Test
    public void giftBalance() throws ApiException {
        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftBalanceManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void loyaltyBalanceManual() throws ApiException {
        GiftCard card = new GiftCard();
        card.setNumber("5022440000000000098");

        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withPaymentMethod(card)
                .withCurrency(CurrencyType.Points)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void giftBalanceNoCurrency() throws ApiException {
        device.balance()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(null)
                .execute();
    }
    //</editor-fold>

    //<editor-fold desc="Certification">
    @Test
    public void test_case_15a() throws ApiException {
        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal("10"), response.getBalanceAmount());
    }

    @Test
    public void test_case_15b() throws ApiException {
        TerminalResponse response = device.addValue()
                .withAmount(new BigDecimal("8"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_case_15c() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("1"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    //</editor-fold>
}
