package com.global.api.tests.terminals.upa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

public class UpaBatchTests {
    IDeviceInterface device;

    public UpaBatchTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.101");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_SATURN_1000);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    @Test
    public void EndOfDay() throws ApiException {
        try {
            IEODResponse response = device.endOfDay();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertNotNull(response.getBatchId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }
}
