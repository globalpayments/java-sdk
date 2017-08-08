package com.global.api.tests.terminals.heartsip;

import com.global.api.ServicesConfig;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;

import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class HsipAdminTests {
    private IDeviceInterface device;
    private String expectedMessage = "";

    public HsipAdminTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HSIP_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.130");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(10000);

        ServicesConfig config = new ServicesConfig();
        config.setSecretApiKey("skapi_cert_MTyMAQBiHVEAewvIzXVFcmUd2UcyBge_eCpaASUp0A");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setDeviceConnectionConfig(deviceConfig);

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                if(!expectedMessage.equals(""))
                    assertEquals(expectedMessage, message);
            }
        });
    }

    @After
    public void tearDown() throws ApiException {
        this.expectedMessage = "";
        //device.reset();
    }

    @Test
    public void cancel() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reset</Request></SIP>";
        device.cancel();
    }

    @Test
    public void initialize() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>GetAppInfoReport</Request></SIP>";

        IInitializeResponse response = device.initialize();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertNotNull(response.getSerialNumber());
    }

    @Test
    public void openLane() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneOpen</Request></SIP>";

        IDeviceResponse response = device.openLane();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void closeLane() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>LaneClose</Request></SIP>";

        IDeviceResponse response = device.closeLane();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void reset() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reset</Request></SIP>";

        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test @Ignore
    public void reboot() throws ApiException {
        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>Reboot</Request></SIP>";
        device.reboot();
    }

    @Test
    public void batchClose() throws ApiException {
        device.closeLane();

        this.expectedMessage = "<SIP><Version>1.0</Version><ECRId>1004</ECRId><Request>CloseBatch</Request></SIP>";

        IBatchCloseResponse response = device.batchClose();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
}
