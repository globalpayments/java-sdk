package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.SafMode;
import com.global.api.entities.enums.UpdateResourceFileType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.MessageException;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IInitializeResponse;
import com.global.api.terminals.abstractions.ISignatureResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PaxAdminTests {
    private IDeviceInterface device;

    public PaxAdminTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("192.168.51.252");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setRequestLogger(new RequestConsoleLogger());

        device = DeviceService.create(deviceConfig);
        device.setOnMessageSent(System.out::println);
        device.setOnMessageReceived(System.out::println);

        assertNotNull(device);
    }

    @Test
    public void initialize() throws ApiException {
//        device.setOnMessageSent(new IMessageSentInterface() {
//            public void messageSent(String message) {
//                assertNotNull(message);
//                assertTrue(message.startsWith("[STX]A00[FS]1.35[FS][ETX]"));
//            }
//        });

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

    @Test
    @Ignore
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

    @Test
    public void getSignature() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A08[FS]1.35[FS]0[FS][ETX]"));
            }
        });

        ISignatureResponse response = device.getSignatureFile();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
        assertNotNull(response.getSignatureData());
    }

    @Test
    public void promptForSignature() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A20"));
            }
        });

        ISignatureResponse response = device.promptForSignature();
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
        assertNotNull(response.getSignatureData());
    }

    @Test
    public void enableStoreAndForwardMode() throws ApiException {
        IDeviceResponse response = device.setStoreAndForwardMode(SafMode.STAY_OFFLINE);
        assertNotNull(response);
        assertEquals("000000", response.getDeviceResponseCode());
    }

    @Test
    public void UpdateResourceFile() throws ApiException, IOException, URISyntaxException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A18[FS]1.35[FS]"));
            }
        });

        boolean isHTTPConnectionMode = false;
        String path = Paths.get(System.getProperty("user.dir") + "\\src\\test\\java\\com\\global\\api\\tests\\testdata\\SampleImage.zip").toString();

        byte[] fileData = Files.readAllBytes(Paths.get(path));

        IDeviceResponse response = device.updateResource(UpdateResourceFileType.RESOURCE_FILE, fileData, false);
        assertEquals("OK", response.getDeviceResponseText());
        assertNotNull(response);

    }

  //  @SneakyThrows
    @Test
    public void deleteImageFile() throws ApiException {
        device.setOnMessageSent(new IMessageSentInterface() {
            public void messageSent(String message) {
                assertNotNull(message);
                assertTrue(message.startsWith("[STX]A22[FS]1.35[FS]"));
            }
        });


      IDeviceResponse response = device.deleteImage("SampleImage.png");
      assertEquals("OK", response.getDeviceResponseText());
      assertNotNull(response);
  }
}