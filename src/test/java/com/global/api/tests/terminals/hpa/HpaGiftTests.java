package com.global.api.tests.terminals.hpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Test;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.CurrencyType;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;

public class HpaGiftTests {
    private final IDeviceInterface device;

    public HpaGiftTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.39");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(30000);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);

        device.openLane();
    }
    
    @After
    public void waitAndReset() {
        try {
            Thread.sleep(3000);
            device.reset();
        }
        catch(Exception e) { /* NOM NOM */ }
    }
    
    @Test
    public void giftSale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void giftSaleWithInvoiceNumber() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withInvoiceNumber("1234")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltySale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.Gift)
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
        device.sale(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .withCurrency(null)
                .execute();
    }
    
    @Test
    public void giftAddValue() throws ApiException {
        TerminalResponse response = device.addValue(new BigDecimal("10"))
                .withAmount(new BigDecimal("10"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltyAddValue() throws ApiException {
        TerminalResponse response = device.addValue(new BigDecimal("10"))
                .withCurrency(CurrencyType.Points)
                .withAmount(new BigDecimal("8"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftAddValueNoAmount() throws ApiException {
        device.addValue().execute();
    }
    
    @Test(expected = BuilderException.class)
    public void giftAddValueNoCurrency() throws ApiException {
        device.addValue(new BigDecimal("10")).withCurrency(null).execute();
    }
    
    @Test
    public void giftVoid() throws ApiException {
        TerminalResponse saleResponse = device.sale(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.Gift)
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());
        waitAndReset();
        
        TerminalResponse  voidResponse = device.voidTransaction()
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
        device.voidTransaction()
                .withTransactionId(null)
                .execute();
    }
    
    @Test
    public void giftBalance() throws ApiException {
        TerminalResponse response = device.balance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltyBalance() throws ApiException {
        TerminalResponse response = device.balance()
                .withCurrency(CurrencyType.Points)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftBalanceNoCurrency() throws ApiException {
        device.balance().withCurrency(null).execute();
    }
    
    @Test
    public void testCase15a() throws ApiException {
        TerminalResponse response = device.balance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals(new BigDecimal(10), response.getBalanceAmount());
    }
    
    @Test
    public void testCase15b() throws ApiException {
        TerminalResponse response = device.addValue().withAmount(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void testCase15c() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void giftStartCard() throws ApiException {
        IDeviceResponse response = device.startCard(PaymentMethodType.Gift);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

}
