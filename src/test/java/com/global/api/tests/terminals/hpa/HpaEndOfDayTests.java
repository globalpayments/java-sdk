package com.global.api.tests.terminals.hpa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.*;
import com.global.api.terminals.hpa.responses.HeartBeatResponse;
import com.global.api.terminals.messaging.IMessageSentInterface;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HpaEndOfDayTests {
    private final IDeviceInterface device;

    public HpaEndOfDayTests() throws ApiException {
        ConnectionConfig deviceConfig = new ConnectionConfig();
        deviceConfig.setDeviceType(DeviceType.HPA_ISC250);
        deviceConfig.setConnectionMode(ConnectionModes.TCP_IP);
        deviceConfig.setIpAddress("10.12.220.39");
        deviceConfig.setPort(12345);
        deviceConfig.setTimeout(20000);
        deviceConfig.setRequestIdProvider(new RandomIdProvider());
        deviceConfig.setEnableLogging(true);

        device = DeviceService.create(deviceConfig);
        assertNotNull(device);
    }

    @Test
    public void test_000_reset() throws ApiException {
        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_001_openLane() throws ApiException {
        IDeviceResponse response = device.openLane();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_002_CreditTransaction() throws Exception {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_002b_reset() throws ApiException {
        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_003_closeLane() throws ApiException {
        IDeviceResponse response = device.closeLane();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_004_SAF_Mode_On() throws Exception {
        IDeviceResponse response = device.setStoreAndForwardMode(true);
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());

        device.reset();
    }

    @Test
    public void test_005_openLane() throws ApiException {
        IDeviceResponse response = device.openLane();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_006_CreditTransaction() throws Exception {
        TerminalResponse response = device.sale(new BigDecimal(10))
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_006b_reset() throws ApiException {
        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_007_closeLane() throws ApiException {
        IDeviceResponse response = device.closeLane();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }

    @Test
    public void test_008_SAF_Mode_Off() throws Exception {
        IDeviceResponse response = device.setStoreAndForwardMode(false);
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());

        device.reset();
    }

    @Test
    public void test_999_EndOfDay() throws ApiException {
        IEODResponse response = device.endOfDay();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
        assertNotNull(response.getAttachmentResponseText());
        assertNotNull(response.getBatchCloseResponseText());
        assertNotNull(response.getEmvOfflineDeclineResponseText());
        assertNotNull(response.getEmvPDLResponseText());
        assertNotNull(response.getEmvTransactionCertificateResponseText());
        assertNotNull(response.getHeartBeatResponseText());
        assertNotNull(response.getReversalResponseText());
        assertNotNull(response.getSafResponseText());

        // REVERSALS
        IDeviceResponse reversalResponse = response.getReversalResponse();
        assertNotNull(reversalResponse);
        assertEquals("00", reversalResponse.getDeviceResponseCode());

        // OFFLINE DECLINE
        IDeviceResponse emvOfflineDeclineResponse = response.getEmvOfflineDeclineResponse();
        assertNotNull(emvOfflineDeclineResponse);
        assertEquals("00", emvOfflineDeclineResponse.getDeviceResponseCode());

        // TRANSACTION CERTIFICATE
        IDeviceResponse emvTransactionCertificateResponse = response.getEmvTransactionCertificateResponse();
        assertNotNull(emvTransactionCertificateResponse);
        assertEquals("00", emvTransactionCertificateResponse.getDeviceResponseCode());

        // GET ATTACHMENT
        IDeviceResponse attachmentResponse = response.getAttachmentResponse();
        assertNotNull(attachmentResponse);
        assertEquals("00", attachmentResponse.getDeviceResponseCode());

        // BATCH CLOSE
        ArrayList<String> batchCloseResponseCodes = new ArrayList<String>();
        batchCloseResponseCodes.add("00");
        batchCloseResponseCodes.add("2501");

        IDeviceResponse batchCloseResponse = response.getBatchCloseResponse();
        assertNotNull(batchCloseResponse);
        assertTrue(batchCloseResponseCodes.contains(batchCloseResponse.getDeviceResponseCode()));

        // HEART BEAT
        HeartBeatResponse heartBeatResponse = (HeartBeatResponse) response.getHeartBeatResponse();
        assertNotNull(heartBeatResponse);
        assertEquals("00", heartBeatResponse.getDeviceResponseCode());

        // EMV PDL
        IDeviceResponse emvPDLResponse = response.getEmvPDLResponse();
        assertNotNull(emvPDLResponse);
        assertEquals("00", emvPDLResponse.getDeviceResponseCode());

        // SAF RESPONSE
        ISAFResponse safResponse = response.getSAFResponse();
        assertNotNull(safResponse);

        // BATCH REPORT
        IBatchReportResponse batchReport = response.getBatchReportResponse();
        assertNotNull(batchReport);
    }

    @Test
    public void test_999b_reset() throws ApiException {
        IDeviceResponse response = device.reset();
        assertNotNull(response);
        assertEquals(response.getDeviceResponseText(), "00", response.getDeviceResponseCode());
    }
}
