package com.global.api.tests.terminals.hpa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HpaCreditTests {
    private static IDeviceInterface device;

    public HpaCreditTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.0.94");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(30000);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
        device.openLane();
    }
    
    @AfterClass
    public static void tearDown() throws ApiException {
        device.closeLane();
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
    public void creditSale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithSignatureCapture() throws ApiException {
        TerminalResponse  response = device.sale(new BigDecimal("12"))
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
    public void creditAuth() throws ApiException {
        TerminalResponse  response = device.authorize(new BigDecimal("12"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        //WaitAndReset();
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoAmount() throws ApiException {
    	device.authorize(null).execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void captureNoTransactionId() throws ApiException {
    	device.capture().execute();
    }

    @Test
    public void creditRefundByCard() throws ApiException {
        TerminalResponse  returnResponse = device.refund(new BigDecimal("14")).execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getDeviceResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditRefundNoAmount() throws ApiException {
    	device.refund().execute();
    }

    @Test(expected = BuilderException.class)
    public void creditRefundByTransactionIdNoAuthCode() throws ApiException {
    	device.refund(new BigDecimal("13"))
                .withTransactionId("1234567")
                .execute();
    }

    @Test
    public void creditVerify() throws ApiException {
        TerminalResponse  response = device.verify().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVoid() throws ApiException {
        TerminalResponse  saleResponse = device.sale(new BigDecimal("17")).execute();
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
    public void creditVoidNoTransactionId() throws ApiException {
    	device.voidTransaction().execute();
    }
    
    @Test
    public void lostTransactionRecovery() throws ApiException {
        int requestId = new RandomIdProvider().getRequestId();
        TerminalResponse response = device.sale(new BigDecimal("10"))
                .withRequestId(requestId)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        waitAndReset();
        
        TerminalResponse duplicateResponse = device.sale(new BigDecimal("10"))
                .withRequestId(requestId)
                .execute();
        assertNotNull(duplicateResponse);
        assertEquals("00", duplicateResponse.getResponseCode());
        assertEquals(response.getAuthorizationCode(), duplicateResponse.getAuthorizationCode());
    }

    @Test
    public void creditStartCard() throws ApiException {
        IDeviceResponse response = device.startCard(PaymentMethodType.Credit);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
}
