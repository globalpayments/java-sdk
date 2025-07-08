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
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HpaEbtTests {
    private final IDeviceInterface device;
    public HpaEbtTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.39");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(30000);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
        device.openLane();
    }

    @After
    public void waitAndReset() throws Exception {
        Thread.sleep(3000);
        device.reset();
    }

    @Test
    public void ebtPurchase() throws ApiException {
        TerminalResponse response = device.purchase(new BigDecimal("10"))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtBalanceInquiry() throws ApiException {
        TerminalResponse response = device.balance()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void ebtRefund() throws ApiException {
        TerminalResponse response = device.refund(new BigDecimal("10"))
                .withPaymentMethodType(PaymentMethodType.EBT).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void ebtRefundAllowDup() throws ApiException {
    	device.refund()
                .withPaymentMethodType(PaymentMethodType.EBT)
                .withAllowDuplicates(true)
                .execute();
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void ebtCashBenefitWithdrawal() throws ApiException {
    	device.withdrawal(new BigDecimal("10")).execute();
    }
    
    @Test
    public void ebtStartCard() throws ApiException {
        IDeviceResponse response = device.startCard(PaymentMethodType.EBT);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
}
