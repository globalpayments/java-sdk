package com.global.api.tests.terminals.heartsip;

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
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HsipEbtTests {
    private IDeviceInterface device;
    private String expectedMessage = "";

    public HsipEbtTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HSIP_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.130");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(30000);

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
    public void waitAndReset() throws Exception {
        Thread.sleep(3000);
        device.reset();
    }

    @Test
    public void ebtPurchase() throws ApiException {
        TerminalResponse response = device.ebtPurchase(1, new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtBalanceInquiry() throws ApiException {
        TerminalResponse response = device.ebtBalance(6).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtRefund() throws ApiException {
        TerminalResponse response = device.ebtRefund(10, new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtRefundAllowDup() throws ApiException {
        device.ebtRefund(11).withAllowDuplicates(true).execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void ebtCashBenefitWithdrawal() throws ApiException {
        device.ebtWithdrawal(12, new BigDecimal("10")).execute();
    }
}
