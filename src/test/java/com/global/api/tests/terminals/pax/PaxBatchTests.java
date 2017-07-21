package com.global.api.tests.terminals.pax;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.BatchCloseResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxBatchTests {
    private IDeviceInterface device;

    public PaxBatchTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.Pax_S300);
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
    public void batchClose() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]B00[FS]1.35[FS]"));
            }
        });

        IBatchCloseResponse response = device.batchClose();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
        assertNotNull(response.getTotalCount());
        assertNotNull(response.getTotalAmount());
    }
}
