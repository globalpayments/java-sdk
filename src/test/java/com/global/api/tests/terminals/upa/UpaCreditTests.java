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
import com.global.api.terminals.messaging.IMessageSentInterface;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import org.junit.Test;

public class UpaCreditTests {
    IDeviceInterface device;

    public UpaCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.198");
        config.setTimeout(30000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_VERIFONE_T650P);
        config.setConnectionMode(ConnectionModes.TCP_IP);

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(new IMessageSentInterface() {
            @Override
            public void messageSent(String message) {
                System.out.println(message);
            }
        });
    }

    @Test
    public void creditSaleSwipe() throws ApiException
    {
        try {
            TerminalResponse response = device.creditSale(new BigDecimal("12.01"))
                .withGratuity(new BigDecimal("0.00"))
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
                .withRequestId(1202)
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
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .withClerkId(420)
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

    /**
     * Procedure: press the red 'X' button on the terminal when the terminal display prompts to present the card
     *
     * @throws ApiException
     */
    @Test
    public void reverseTerminalTrans() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.34"))
            .execute();

        assertNotNull(response);
        assertEquals("Failed", response.getStatus());
        assertEquals("APP001", response.getDeviceResponseCode());
        assertEquals("TRANSACTION CANCELLED BY USER", response.getResponseText());
    }

    @Test
    public void incrementalAuths() throws ApiException
    { // doesn't seem to work as described in 1.30 docs
        TerminalResponse response1 = device.creditAuth(new BigDecimal("10.00"))
                .execute();

        TerminalResponse response2 = device.creditAuth(new BigDecimal("5.00"))
                .withTransactionId(response1.getTransactionId())
                .execute();

        assertNotNull(response2);
        assertEquals("00", response2.getResponseCode());
    }

    @Test
    public void cancelledTrans() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.34"))
                .withGratuity(new BigDecimal("0.00"))
                .execute();

        assertNotNull(response);
        assertEquals("Failed", response.getStatus());
        assertEquals("APP001", response.getDeviceResponseCode());
        assertEquals("TRANSACTION CANCELLED BY USER", response.getDeviceResponseText());
    }
}
