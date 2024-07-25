package com.global.api.tests.terminals.upa;

import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import com.global.api.logging.RequestFileLogger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaBatchTests {
    IDeviceInterface device;

    public UpaBatchTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.199");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

//        config.setRequestLogger(new RequestFileLogger("batchTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void test01_prepareBatch() throws ApiException {
        assertTrue(device.getBatchSummary().getDeviceResponseText().equalsIgnoreCase("EMPTY BATCH"));

        // use Visa card
        runBasicTests(
            device.creditSale(new BigDecimal("12.01"))
                    .withGratuity(new BigDecimal("0.00"))
                    .execute()
        );

        // use Amex card
        runBasicTests(
            device.creditAuth(new BigDecimal("10.00"))
                    .execute()
        );
    }

    @Test
    public void test02_BatchSummaryReport() throws ApiException {
        IBatchReportResponse response = device.getBatchSummary();

        runBasicTests(response);
        assertNotNull(response.getBatchSummary().getBatchId());
        assertEquals("1", response.getBatchSummary().getTransactionCount().toString());
        assertEquals(new BigDecimal("12.01"), response.getBatchSummary().getTotalAmount());
        assertNotNull(response.getBatchSummary().getOpenTime());
        assertNotNull(response.getBatchSummary().getOpenTransactionId());
        assertNotNull(response.getVisaSummary());
        assertNull(response.getMastercardSummary());
    }

    @Test
    public void test03_BatchDetailsReport() throws ApiException {
        IBatchReportResponse response = device.getBatchDetails();

        runBasicTests(response);
        assertNotNull(response.getBatchSummary().getBatchId());
        assertEquals("1", response.getBatchSummary().getTransactionCount().toString());
        assertEquals(new BigDecimal("12.01"), response.getBatchSummary().getTotalAmount());
        assertNotNull(response.getBatchSummary().getOpenTime());
        assertNotNull(response.getBatchSummary().getOpenTransactionId());

        ArrayList<TransactionSummary> transactions = response.getTransactionSummaries();
        transactions.forEach((n) -> {
            assertNotNull(n.getTransactionType());
            assertNotNull(n.getTransactionId());
            assertNotNull(n.getAmount());
        });
    }

    @Test
    public void test04_OpenTabDetailsReport() throws ApiException {
        IBatchReportResponse response = device.getOpenTabDetails();

        runBasicTests(response);

        ArrayList<TransactionSummary> transactions = response.getTransactionSummaries();

        transactions.forEach((n) -> {
            try {
                runBasicTests(
                    device.creditCapture(n.getAuthorizedAmount())
                            .withGratuity(new BigDecimal("0.00"))
                            .withTerminalRefNumber(n.getTransactionId())
                            .withTransactionId(n.getTransactionId())
                            .execute()
                );
            } catch (ApiException e) {
                System.out.println("failed: " + n.getTransactionId());
            }
        });
    }

    @Test
    public void test05_EndOfDay() throws ApiException {
        IEODResponse response = device.endOfDay();

        runBasicTests(response);
        assertNotNull(response.getBatchId());
    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
}
