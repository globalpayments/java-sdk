package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxDebitTests {
    private final IDeviceInterface device;

    public PaxDebitTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.51.252");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void debitSale() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void debitSaleNoAmount() throws ApiException {
        device.sale(null)
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();
    }

    @Test
    public void debitRefund() throws ApiException {
        TerminalResponse response = device.refund(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitRefundByTransactionId() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(11))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse response2 = device.refund(new BigDecimal(11))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void debitRefundNoAmount() throws ApiException {
        device.refund().execute();
    }

    @Test
    public void debitVoidWithTransactionId() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse voidResponse = device.voidTransaction()
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withReferenceNumber(response.getTransactionId())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test
    public void debitVoidWithTerminalReferenceNumber() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        TerminalResponse voidResponse = device.voidTransaction()
                .withPaymentMethodType(PaymentMethodType.Debit)
                .withTerminalRefNumber(response.getTerminalRefNumber())
                .execute();
        assertNotNull(voidResponse);
        assertEquals("00", voidResponse.getResponseCode());
    }
}
