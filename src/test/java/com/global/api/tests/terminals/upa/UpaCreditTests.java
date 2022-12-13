package com.global.api.tests.terminals.upa;

import java.math.BigDecimal;

import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.tests.terminals.hpa.RandomIdProvider;

import com.global.api.utils.RequestFileLogger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaCreditTests {
    IDeviceInterface device;

    public UpaCreditTests() throws ApiException {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8081);
        config.setIpAddress("192.168.0.198");
        config.setTimeout(45000);
        config.setRequestIdProvider(new RandomIdProvider());
        config.setDeviceType(DeviceType.UPA_DEVICE);
        config.setConnectionMode(ConnectionModes.TCP_IP);

//        config.setRequestLogger(new RequestFileLogger("creditTests.txt"));

        device = DeviceService.create(config);
        assertNotNull(device);

        device.setOnMessageSent(System.out::println);
    }

    @Test
    public void creditSaleSwipe() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.01"))
            .withGratuity(new BigDecimal("0.00"))
            .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.01"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleChip() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.02"))
            .withRequestId(1202)
            .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.02"), response.getTransactionAmount());
    }

    @Test
    public void creditSaleContactless() throws ApiException
    {
        TerminalResponse response = device.creditSale(new BigDecimal("12.03"))
            .execute();

        runBasicTests(response);
        assertEquals(new BigDecimal("12.03"), response.getTransactionAmount());
    }

    @Test
    public void CardVerify() throws ApiException
    {
        // use Visa card
        TerminalResponse response = device.creditVerify()
            .withCardBrandStorage(StoredCredentialInitiator.Merchant)
            .withRequestMultiUseToken(true)
            .withClerkId(1234)
            .execute();

        runBasicTests(response);
        assertNotNull(response.getToken()); // will fail if MUTs aren't enabled
    }

    @Test
    public void BalanceInquiry() throws ApiException {
        TerminalResponse response = device.ebtBalance()
            .execute();

        runBasicTests(response);
        assertNotNull(response.getBalanceAmount());
    }

    @Test
    public void BlindRefund() throws ApiException
    {
        runBasicTests(
            device.creditRefund(new BigDecimal("1.23"))
                    .execute()
        );
    }

    @Test
    public void TipAdjust() throws ApiException
    {
        TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
            .execute();

        runBasicTests(response1);

        TerminalResponse response2 = device.tipAdjust(new BigDecimal("1.50"))
            .withTerminalRefNumber(response1.getTerminalRefNumber())
            .withClerkId(420)
            .execute();

        runBasicTests(response2);
        assertEquals(new BigDecimal("1.50"), response2.getTipAmount());
        assertEquals(new BigDecimal("13.84"), response2.getTransactionAmount());
    }

    @Test
    public void VoidTerminalTrans() throws ApiException
    {
        TerminalResponse response1 = device.creditSale(new BigDecimal("12.34"))
            .withGratuity(new BigDecimal("0.00"))
            .execute();

        runBasicTests(response1);

        runBasicTests(
            device.creditVoid()
                .withTerminalRefNumber(response1.getTerminalRefNumber())
                .execute()
        );
    }

    /**
     * Procedure: press the red 'X' button on the terminal when the terminal display prompts to present the card
     */
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

    public void runBasicTests(IDeviceResponse response) {
        assertNotNull(response);
        assertEquals("00", response.getDeviceResponseCode());
        assertTrue(response.getStatus().equalsIgnoreCase("Success"));
    }
}
