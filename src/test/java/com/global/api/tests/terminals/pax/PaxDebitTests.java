package com.global.api.tests.terminals.pax;

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
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxDebitTests {
    private IDeviceInterface device;
    private String rec_message;

    public PaxDebitTests() throws ApiException {
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

    @Test
    public void debitSale() throws ApiException {
        rec_message = "[STX]T02[FS]1.35[FS]01[FS]1000[FS][US][US][US][US][US]1[FS]5[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.debitSale(5, new BigDecimal(10))
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
        rec_message = "[STX]T02[FS]1.35[FS]02[FS]1000[FS][FS]6[FS][FS][ETX]";
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response = device.debitRefund(6, new BigDecimal(10)).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitRefundByTransactionId() throws ApiException {
        TerminalResponse response = device.debitSale(5, new BigDecimal(11))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        rec_message = String.format("[STX]T02[FS]1.35[FS]02[FS]1100[FS][FS]5[FS][FS]HREF=%s[ETX]", response.getTransactionId());
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith(rec_message));
            }
        });

        TerminalResponse response2 = device.debitRefund(5, new BigDecimal(11))
                .withTransactionId(response.getTransactionId())
                .execute();
        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }


    @Test(expected = BuilderException.class)
    public void debitRefundNoAmount() throws ApiException {
        device.debitRefund(5).execute();
    }
}
