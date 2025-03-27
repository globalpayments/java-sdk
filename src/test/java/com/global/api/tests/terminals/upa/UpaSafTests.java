package com.global.api.tests.terminals.upa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.upa.responses.UpaSafResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpaSafTests {
    IDeviceInterface device;

    private UpaSafTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.51.94");
        config.setTimeout(45_000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        device = DeviceService.create(config);
        Assertions.assertNotNull(device);

        device.setOnMessageSent(System.out::println);
        device.setOnMessageReceived(System.out::println);
    }

    @Test
    public void safSummaryReport() throws ApiException {
        UpaSafResponse response = (UpaSafResponse) device.safSummaryReport("", "");
        Assertions.assertNotNull(response);
    }
}
