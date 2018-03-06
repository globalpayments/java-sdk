package com.global.api.tests.terminals.heartsip;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HsipCreditTests {
    private static IDeviceInterface device;
    private String expectedMessage = "";

    public HsipCreditTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HSIP_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.130");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(30000);

        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setDeviceConnectionConfig(deviceConfig);

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(!expectedMessage.equals(""))
                    assertEquals(expectedMessage, message);
            }
        });
        device.openLane();
    }

    @AfterClass
    public static void tearDown() throws ApiException {
        device.closeLane();
    }

    @After
    public void waitAndReset() throws ApiException {
        try {
            Thread.sleep(3000);
            device.reset();
        }
        catch(Exception e) {
            // nom nom
        }
    }

    @Test
    public void creditSale() throws ApiException {
        TerminalResponse response = device.creditSale(1, new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditSaleWithSignatureCapture() throws ApiException {
        TerminalResponse  response = device.creditSale(1, new BigDecimal("12"))
                .withSignatureCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditSaleNoAmount() throws ApiException {
        device.creditSale(1).execute();
    }

    @Test
    public void creditAuth() throws ApiException {
        TerminalResponse  response = device.creditAuth(1, new BigDecimal("12"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        //WaitAndReset();

        //TerminalResponse captureResponse = device.creditCapture(2, new BigDecimal("12"))
        //    .withTransactionId(response.TransactionId)
        //    .execute();
        //assertNotNull(captureResponse);
        //assertEquals("00", captureResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditAuthNoAmount() throws ApiException {
        device.creditAuth(1).execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void captureNoTransactionId() throws ApiException {
        device.creditCapture(1).execute();
    }

    @Test
    public void creditRefundByCard() throws ApiException {
        TerminalResponse  returnResponse = device.creditRefund(2, new BigDecimal("14")).execute();
        assertNotNull(returnResponse);
        assertEquals("00", returnResponse.getDeviceResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditRefundNoAmount() throws ApiException {
        device.creditRefund(1).execute();
    }

    @Test(expected = BuilderException.class)
    public void creditRefundByTransactionIdNoAuthCode() throws ApiException {
        device.creditRefund(2, new BigDecimal("13"))
                .withTransactionId("1234567")
                .execute();
    }

    @Test
    public void creditVerify() throws ApiException {
        TerminalResponse  response = device.creditVerify(1).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void creditVoid() throws ApiException {
        TerminalResponse  saleResponse = device.creditSale(12, new BigDecimal("17")).execute();
        assertNotNull(saleResponse);
        assertEquals("00", saleResponse.getResponseCode());
        waitAndReset();

        TerminalResponse  voidResponse = device.creditVoid(1)
                .withTransactionId(saleResponse.getTransactionId())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void creditVoidNoTransactionId() throws ApiException {
        device.creditVoid(1).execute();
    }
}
