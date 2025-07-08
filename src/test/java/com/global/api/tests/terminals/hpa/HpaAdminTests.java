package com.global.api.tests.terminals.hpa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.SendFileType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.*;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import static org.junit.Assert.*;

public class HpaAdminTests {
    private final IDeviceInterface device;

    public HpaAdminTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.0.94");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(120000);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }
    
    @Test
    public void cancel() throws ApiException {
        device.cancel();
    }

    @Test
    public void initialize() throws ApiException {
        IInitializeResponse response = device.initialize();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertNotNull(response.getSerialNumber());
    }

    @Test
    public void openLane() throws ApiException {
        IDeviceResponse response = device.openLane();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void closeLane() throws ApiException {
        IDeviceResponse response = device.closeLane();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test
    public void reset() throws ApiException {
        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test @Ignore
    public void reboot() throws ApiException {
        device.reboot();
    }

    @Test
    public void batchClose() throws ApiException {
        device.closeLane();
        IBatchCloseResponse response = device.batchClose();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }

    @Test(expected = UnsupportedTransactionException.class)
    public void getSignatureDirect() throws ApiException {
        device.getSignatureFile();
    }

    @Test
    public void getSignatureIndirect() throws ApiException {
        TerminalResponse response = device.sale(new BigDecimal("120"))
                .withSignatureCapture(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("0", response.getSignatureStatus());
        assertNotNull(response.getSignatureData());
    }

    @Test
    public void promptForSignature() throws ApiException {
        device.openLane();
        ISignatureResponse response = device.promptForSignature();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertNotNull(response.getSignatureData());

        device.reset();
        device.closeLane();
    }
    
    @Test
    public void addLineItem() throws ApiException {
        IDeviceResponse response = device.lineItem("Green Beans", null, null, null);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
    
    @Test
    public void addLineItemWithParams() throws ApiException {
        IDeviceResponse response = device.lineItem("Green Beans", "$0.59", "TOTAL", "$1.19");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
    
    @Test
    public void sendStoreAndForward() throws ApiException {
        ISAFResponse response = device.sendStoreAndForward();
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
    
    @Test
    public void enableStoreAndForwardMode() throws ApiException {
        IDeviceResponse response = device.setStoreAndForwardMode(true);
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
    
    @Test
    public void sendFile() throws ApiException {
        IDeviceResponse response = device.sendFile(SendFileType.Logo,"C:\\temp\\IDLELOGO.jpg");
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
    }
}
