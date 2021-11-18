package com.global.api.tests.terminals.upa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

public class UpaCreditTests {
    IDeviceInterface device;

    public UpaCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.101");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_SATURN_1000);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        device = DeviceService.create(config);
        assertNotNull(device);
    }

    @Test
    public void creditSaleSwipe() throws ApiException
    {
        try {
            TerminalResponse response = device.creditSale(new BigDecimal("12.01"))
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void creditSaleChip() throws ApiException
    {
        try {
            TerminalResponse response = device.creditSale(new BigDecimal("12.02"))
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertEquals(new BigDecimal("12.02"), response.getTransactionAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void creditSaleContactless() throws ApiException
    {
        try {
            TerminalResponse response = device.creditSale(new BigDecimal("12.03"))
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertEquals(new BigDecimal("12.03"), response.getTransactionAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void CardVerify() throws ApiException
    {
        // MUT generation is dependent on the test account in use
        try {
            TerminalResponse response = device.creditVerify()
                .withRequestMultiUseToken(true)
                .withClerkId(1234)
                .execute();

            assertNotNull(response);
            assertNotNull(response.getToken()); // will fail if MUTs aren't enabled
            assertEquals("85", response.getResponseCode()); // used Discover
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void BalanceInquiry() throws ApiException {
        try {
            TerminalResponse response = device.ebtBalance()
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
            assertNotNull(response.getBalanceAmount());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void RefundToCard() throws ApiException
    {
        try {
            TerminalResponse response = device.creditRefund(new BigDecimal("1.23"))
                .execute();

            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void TipAdjust() throws ApiException
    {
        try {
            TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
                .execute();

            TerminalResponse response2 = device.tipAdjust(new BigDecimal("1.50"))
                .withTransactionId(response1.getTransactionId())
                .execute();

            assertNotNull(response2);
            assertEquals("00", response2.getResponseCode());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }

    @Test
    public void VoidTerminalTrans() throws ApiException
    {
        try {
            TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
                .execute();

            TerminalResponse response2 = device.creditVoid()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute();

            assertNotNull(response2);
            assertEquals("00", response2.getResponseCode());
        } catch (Exception e) {
            device = null;
            System.out.println(e.getMessage());
            throw new ApiException(e.getMessage());
        }
    }
}
