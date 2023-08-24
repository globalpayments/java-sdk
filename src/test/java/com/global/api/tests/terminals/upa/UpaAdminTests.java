package com.global.api.tests.terminals.upa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.upa.subgroups.RegisterPOS;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaAdminTests {
    IDeviceInterface device;

    public UpaAdminTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.198");
        config.setTimeout(450000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

//        config.setRequestLogger(new RequestFileLogger("AdminTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void test01_Ping() throws ApiException {
        IDeviceResponse response = device.ping();

        runBasicTests(response);
    }

    @Test
    public void test02_cancel() {
        try {
            device.cancel();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void test03_lineItems() throws ApiException {
        runBasicTests(device.addLineItem("Line Item 1", "111.11"));
        runBasicTests(device.addLineItem("Line Item 2", null));
        runBasicTests(device.addLineItem("Line Item 3", "333.33"));

        try {
            device.cancel();
        } catch (Exception e) {
            fail();
        }
}

    @Test
    public void test04_reboot() throws ApiException {
        runBasicTests(device.reboot());
    }

    @Test
    public void test05_sendReady() throws ApiException {
        device.sendReady();
    }

    @Test
    public void test_registerPOS() throws ApiException{
        RegisterPOS data = new RegisterPOS();
        data.setAppName("com.global.testapp");
        data.setLaunchOrder(1);
        data.setRemove(false);

        IDeviceResponse response = device.registerPOS(data);
        runBasicTests(response);
    }

    @Test
    public void test_registerPOS_O1() throws ApiException{
        RegisterPOS data = new RegisterPOS();
        data.setAppName("com.global.testapp");
        data.setLaunchOrder(1);
        data.setRemove(true);

        IDeviceResponse response = device.registerPOS(data);
        runBasicTests(response);
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
}
