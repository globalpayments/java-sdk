package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaxLevel2Tests {
    private final IDeviceInterface device;

    public PaxLevel2Tests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    // PoNumber
    @Test
    public void CheckPoNumber() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("10")).withPoNumber("123456789").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // CustomerCode
    @Test
    public void CheckCustomerCode() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("11"))
                .withCustomerCode("123456789")
                .withTaxAmount(new BigDecimal("1.22"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // TaxExempt
    @Test
    public void CheckTaExemptTrue() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("12"))
                .withCustomerCode("123456789")
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void CheckTaxExemptFalse() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("13"))
                .withTaxAmount(new BigDecimal("1.22"))
                .withCustomerCode("987654321")
                .withTaxType(TaxType.SalesTax)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // TaxExemptId
    @Test
    public void CheckTaxExemptId() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("14"))
                .withCustomerCode("987654321")
                .withTaxType(TaxType.TaxExempt, "987654321")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // All fields
    @Test
    public void checkAllFields() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("15"))
                .withPoNumber("123456789")
                .withCustomerCode("8675309")
                .withTaxType(TaxType.TaxExempt, "987654321").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
