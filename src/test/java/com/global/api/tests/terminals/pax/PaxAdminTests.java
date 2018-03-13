package com.global.api.tests.terminals.pax;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PaxAdminTests {
    private IDeviceInterface device;

    public PaxAdminTests() throws ApiException {
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
    public void initialize() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A00[FS]1.35[FS][ETX]"));
            }
        });

        IInitializeResponse response = device.initialize();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
        assertNotNull(response.getSerialNumber());
    }

    @Test(expected = MessageException.class)
    public void cancel() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A14[FS]1.35[FS][ETX]"));
            }
        });

        device.cancel();
    }

    @Test
    public void reset() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A16[FS]1.35[FS][ETX]"));
            }
        });

        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
    }

    @Test @Ignore
    public void reboot() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A26[FS]1.35[FS][ETX]"));
            }
        });

        IDeviceResponse response = device.reboot();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
    }
}