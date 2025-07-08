package com.global.api.tests.terminals.upa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

public class UpaEbtTests {
    IDeviceInterface device;

    public UpaEbtTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.8.181");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);
        config.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    @Test
    public void ebtSale() throws ApiException
    {
        try {
            TerminalResponse response = device.purchase(new BigDecimal("12.01"))
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void ebtBalanceInquiry() throws ApiException
    {
        try {
            TerminalResponse response = device.balance()
                    .withPaymentMethodType(PaymentMethodType.EBT)
                    .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertNotNull(response.getBalanceAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void ebtRefund() throws ApiException
    { 
        // tests currently result in RspCode 02; RspText CALL;
        // I think this is due to device/gateway config, not an issue with this SDK
        try {
            TerminalResponse response = device.refund(new BigDecimal("1.23"))
                    .withPaymentMethodType(PaymentMethodType.EBT)
                    .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }
}
