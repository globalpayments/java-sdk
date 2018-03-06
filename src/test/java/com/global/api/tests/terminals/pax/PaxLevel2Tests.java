package com.global.api.tests.terminals.pax;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.TaxType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxLevel2Tests {
    private IDeviceInterface device;

    public PaxLevel2Tests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_S300);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);

        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setDeviceConnectionConfig(deviceConfig);

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    // PoNumber
    @Test
    public void CheckPoNumber() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1000[FS][FS]1[FS][FS][FS]123456789[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("10")).withPoNumber("123456789").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // CustomerCode
    @Test
    public void CheckCustomerCode() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1100[US][US][US][US]122[FS][FS]1[FS][FS][FS][US]123456789[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("11"))
                .withCustomerCode("123456789")
                .withTaxAmount(new BigDecimal("1.22"))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // TaxExempt
    @Test
    public void CheckTaxExcemptTrue() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1200[FS][FS]1[FS][FS][FS][US]123456789[US]1[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("12"))
                .withCustomerCode("123456789")
                .withTaxType(TaxType.TaxExempt)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void CheckTaxExcemptFalse() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1300[US][US][US][US]122[FS][FS]1[FS][FS][FS][US]987654321[US]0[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("13"))
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
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1400[FS][FS]1[FS][FS][FS][US]987654321[US]1[US]987654321[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("14"))
                .withCustomerCode("987654321")
                .withTaxType(TaxType.TaxExempt, "987654321")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    // All fields
    @Test
    public void checkAllFields() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]T00[FS]1.35[FS]01[FS]1500[FS][FS]1[FS][FS][FS]123456789[US]8675309[US]1[US]987654321[FS][FS][ETX]"));
            }
        });

        TerminalResponse response = device.creditSale(1, new BigDecimal("15"))
                .withPoNumber("123456789")
                .withCustomerCode("8675309")
                .withTaxType(TaxType.TaxExempt, "987654321").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
