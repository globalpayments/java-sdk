package com.global.api.tests.terminals.upa;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.upa.Entities.Enums.UpaCardTypeFilter;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import com.global.api.logging.RequestFileLogger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaGiftTests {
    IDeviceInterface device;

    public UpaGiftTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.105");
        config.setTimeout(15000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        config.setRequestLogger(new RequestFileLogger("C:\\Temp\\giftTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void giftAddValue() throws ApiException {
        TerminalResponse response = device.giftAddValue(new BigDecimal("10.01"))
            .withGiftTransactionType(TransactionType.Sale)
            .execute();

        runBasicTests(response);
        
        assertEquals("5022440000000000007", response.getUnmaskedCardNumber());
    }

    //Positive Scenarios
    @Test
    public void giftCardTypeFilter01() throws ApiException {
        TerminalResponse response = device.giftAddValue(new BigDecimal("10.01"))
                .withGiftTransactionType(TransactionType.Sale)
                .withCardTypeFilter(EnumSet.allOf(UpaCardTypeFilter.class))
                .execute();

        runBasicTests(response);

    }

    @Test
    public void giftCardTypeFilter02() throws ApiException {
        TerminalResponse response = device.giftAddValue(new BigDecimal("10.01"))
                .withGiftTransactionType(TransactionType.Sale)
                .withCardTypeFilter(EnumSet.of(UpaCardTypeFilter.VISA,UpaCardTypeFilter.AMEX,UpaCardTypeFilter.DISCOVER))
                .execute();

        runBasicTests(response);

    }

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }

}
