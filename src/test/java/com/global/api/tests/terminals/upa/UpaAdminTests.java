package com.global.api.tests.terminals.upa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

public class UpaAdminTests {
    IDeviceInterface device;

    public UpaAdminTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.101");
        config.setTimeout(20000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_SATURN_1000);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    @Test
    public void Ping() throws ApiException {
        IDeviceResponse response = device.ping();

        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void cancel() throws ApiException {
        try {
            device.cancel();

            // assertNotNull(response);
            // assertEquals("00", response.getDeviceResponseCode());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void reboot() throws ApiException {
        try {
            IDeviceResponse response = device.reboot();

            assertNotNull(response);
            assertEquals("00", response.getDeviceResponseCode());

            Thread.sleep(100000); // device is unavailable for about 70 seconds after a reboot
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void lineItems() throws ApiException {
        try {
            IDeviceResponse response1 = device.addLineItem("Line Item 1", "111.11");
            assertNotNull(response1);
            assertEquals("00", response1.getDeviceResponseCode());

            IDeviceResponse response2 = device.addLineItem("Line Item 2", null);
            assertNotNull(response2);
            assertEquals("00", response2.getDeviceResponseCode());

            IDeviceResponse response3 = device.addLineItem("Line Item 3", "333.33");
            assertNotNull(response3);
            assertEquals("00", response3.getDeviceResponseCode());

            device.cancel();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }    
}
