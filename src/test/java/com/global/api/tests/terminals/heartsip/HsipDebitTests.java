package com.global.api.tests.terminals.heartsip;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HsipDebitTests {
    private static IDeviceInterface device;
    private String expectedMessage = "";

    public HsipDebitTests() throws ApiException {
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

    @After
    public void waitAndReset() throws Exception {
        Thread.sleep(3000);
        device.reset();
    }

    @Test
    public void debitSale() throws ApiException {
        TerminalResponse response = device.debitSale(5, new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void debitSaleNoAmount() throws ApiException {
        device.debitSale(5).execute();
    }

    @Test
    public void debitRefund() throws ApiException {
        TerminalResponse response = device.debitRefund(6, new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void debitRefund_NoAmount() throws ApiException {
        device.debitRefund(5).execute();
    }
}
