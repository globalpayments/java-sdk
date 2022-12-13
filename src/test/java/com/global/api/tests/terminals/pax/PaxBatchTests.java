package com.global.api.tests.terminals.pax;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.SafDelete;
import com.global.api.entities.enums.SafReportSummary;
import com.global.api.entities.enums.SafUpload;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.terminals.pax.responses.SAFDeleteResponse;
import com.global.api.terminals.pax.responses.SAFSummaryReport;
import com.global.api.terminals.pax.responses.SAFUploadResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PaxBatchTests {
    private IDeviceInterface device;

    public PaxBatchTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.PAX_DEVICE);
        deviceConfig.setConnectionMode(ConnectionModes.HTTP);
        deviceConfig.setIpAddress("10.12.220.172");
        deviceConfig.setPort(10009);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());

        device = DeviceService.create(deviceConfig);
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
    
    @Test
    public void safUpload() throws ApiException{
        SAFUploadResponse response = device.safUpload(SafUpload.ALL_TRANSACTION);
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
    }
    
    @Test
    public void safDelete() throws ApiException{
        SAFDeleteResponse response = device.safDelete(SafDelete.FAILED_TRANSACTION_RECORD);
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
    }
    
    @Test
    public void safSummaryReport() throws ApiException{
        SAFSummaryReport response = device.safSummaryReport(SafReportSummary.ALL_REPORT);
        assertNotNull(response);
        assertEquals("OK", response.getDeviceResponseText());
    }
}
