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
import com.global.api.terminals.messaging.IMessageSentInterface;

public class HpaGiftTests {
    private static IDeviceInterface device;
    private String expectedMessage = "";

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

        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(!expectedMessage.equals(""))
                    assertEquals(expectedMessage, message);
            }
        });
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
        TerminalResponse response = device.giftSale(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void giftSaleWithInvoiceNumber() throws ApiException {
        TerminalResponse response = device.giftSale(new BigDecimal("10"))
                .withInvoiceNumber("1234")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltySale() throws ApiException {
        TerminalResponse response = device.giftSale(new BigDecimal("10"))
                .withCurrency(CurrencyType.Points)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftSaleNoAmount() throws ApiException {
        device.giftSale().execute();
    }
    
    @Test(expected = BuilderException.class)
    public void giftSaleNoCurrency() throws ApiException {
        device.giftSale(new BigDecimal("10")).withCurrency(null).execute();
    }
    
    @Test
    public void giftAddValue() throws ApiException {
        TerminalResponse response = device.giftAddValue(new BigDecimal("10"))
                .withAmount(new BigDecimal("10"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltyAddValue() throws ApiException {
        TerminalResponse response = device.giftAddValue(new BigDecimal("10"))
                .withCurrency(CurrencyType.Points)
                .withAmount(new BigDecimal("8"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftAddValueNoAmount() throws ApiException {
        device.giftAddValue().execute();
    }
    
    @Test(expected = BuilderException.class)
    public void giftAddValueNoCurrency() throws ApiException {
        device.giftAddValue(new BigDecimal("10")).withCurrency(null).execute();
    }
    
    @Test
    public void giftVoid() throws ApiException {
        TerminalResponse saleResponse = device.giftSale()
                .withAmount(new BigDecimal("10"))
                .execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());
        waitAndReset();
        
        TerminalResponse  voidResponse = device.giftVoid()
                .withTransactionId(saleResponse.getTransactionId())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftVoidNoCurrency() throws ApiException {
        device.giftVoid().withCurrency(null).withTransactionId("1").execute();
    }
    
    @Test(expected = BuilderException.class)
    public void giftVoidNoTransactionId() throws ApiException {
        device.giftVoid().withTransactionId(null).execute();
    }
    
    @Test
    public void giftBalance() throws ApiException {
        TerminalResponse response = device.giftBalance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void loyaltyBalance() throws ApiException {
        TerminalResponse response = device.giftBalance()
                .withCurrency(CurrencyType.Points)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test(expected = BuilderException.class)
    public void giftBalanceNoCurrency() throws ApiException {
        device.giftBalance().withCurrency(null).execute();
    }
    
    @Test
    public void testCase15a() throws ApiException {
        TerminalResponse response = device.giftBalance().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertEquals("10", response.getBalanceAmount());
    }
    
    @Test
    public void testCase15b() throws ApiException {
        TerminalResponse response = device.giftAddValue().withAmount(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    
    @Test
    public void testCase15c() throws ApiException {
        TerminalResponse response = device.giftSale(new BigDecimal("10")).execute();
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
