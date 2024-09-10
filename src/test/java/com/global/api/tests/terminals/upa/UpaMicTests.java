package com.global.api.tests.terminals.upa;

import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.ConnectionModes;
import com.global.api.entities.enums.DeviceType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.DeviceService;
import com.global.api.terminals.ConnectionConfig;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IDeviceInterface;
import com.global.api.terminals.abstractions.IDeviceResponse;
import com.global.api.terminals.abstractions.IEODResponse;
import com.global.api.tests.gpapi.BaseGpApiTest;
import com.global.api.tests.terminals.hpa.RandomIdProvider;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpaMicTests extends BaseGpApiTest {
    RequestConsoleLogger _logManagementProvider;
    private final IDeviceInterface device;

    private final BigDecimal amount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("10"), 2);

    public UpaMicTests() throws ApiException {
        _logManagementProvider = new RequestConsoleLogger();
        GpApiConfig gatewayConfig = getGpApiConfig();

        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.setGatewayConfig(gatewayConfig);
        connectionConfig.setDeviceType(DeviceType.UPA_DEVICE);
        connectionConfig.setConnectionMode(ConnectionModes.MEET_IN_THE_CLOUD);
        connectionConfig.setTimeout(30000);
        connectionConfig.setRequestIdProvider(new RandomIdProvider());
        connectionConfig.setLogManagementProvider(_logManagementProvider);

        device = DeviceService.create(connectionConfig);
    }

    private static GpApiConfig getGpApiConfig() {
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("9187");

        GpApiConfig gatewayConfig = new GpApiConfig();
        gatewayConfig.setAppId(MITC_UPA_APP_ID);
        gatewayConfig.setAppKey(MITC_UPA_APP_KEY);
        gatewayConfig.setChannel(Channel.CardPresent);
        gatewayConfig.setCountry("CA");
        gatewayConfig.setDeviceCurrency("CAD");
        gatewayConfig.setEnableLogging(true);
        gatewayConfig.setAccessTokenInfo(accessTokenInfo);
        return gatewayConfig;
    }

    @Test
    public void CreditSale() throws ApiException {
        TerminalResponse response = device.creditSale(amount).execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void CreditSale_WithZeroTip() throws ApiException {
        TerminalResponse response = device.creditSale(amount)
                .withGratuity(new BigDecimal(0)) //Tip screen should not be displayed
                .execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void CreditSale_WithTip() throws ApiException {
        BigDecimal tipAmount = generateRandomBigDecimalFromRange(new BigDecimal("1"), new BigDecimal("2"), 2);

        TerminalResponse response = device.creditSale(amount)
                .withGratuity(tipAmount)
                .execute();

        assertNotNull(response);
        assertEquals(amount.add(tipAmount), response.getAuthorizedAmount());
        assertMitcTransactionResponse(response);
    }

    @Test
    public void LineItem() throws InterruptedException {
        try {
            IDeviceResponse response = device.addLineItem("Line Item #1", "10.00");

            assertNotNull(response);
            assertEquals("00", response.getDeviceResponseCode());
            assertEquals("COMPLETE", response.getDeviceResponseText());
        } catch (ApiException exc) {
            fail(exc.getMessage() + "   " + exc.getMessage());
        }

        Thread.sleep(10000);

        try {
            device.cancel();
        } catch (ApiException exception) {
            assertNull(exception.getMessage());
        }
    }

    @Test
    public void Ping() {
        try {
            device.ping();
        } catch (ApiException exception) {
            assertNull(exception.getMessage());
        }
    }

    @Test
    public void CreditCancel() {
        try {
            device.cancel();
        } catch (ApiException exception) {
            assertNull(exception.getMessage());
        }
    }

    @Test
    public void CreditAuth() throws ApiException {
        TerminalResponse response = device.creditAuth(amount).execute();

        assertNotNull(response);
        assertEquals("TRANSACTION CANCELLED  COMMAND NOT SUPPORTED", response.getDeviceResponseText());
        assertEquals("ERR017", response.getDeviceResponseCode());
        assertEquals("Failed", response.getStatus());
    }

    @Test
    public void CreditAuthCompletion() throws ApiException {
        TerminalResponse capture = device.creditCapture(new BigDecimal(10))
                .withTerminalRefNumber("0215")
                .execute();

        assertNotNull(capture);
        assertEquals("TRANSACTION CANCELLED  COMMAND NOT SUPPORTED", capture.getDeviceResponseText());
        assertEquals("ERR017", capture.getDeviceResponseCode());
        assertEquals("Failed", capture.getStatus());
    }

    @Test
    public void CreditRefund() throws ApiException {
        TerminalResponse response = device.creditRefund(amount).execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void CreditRefund_Linked() throws ApiException, InterruptedException {
        TerminalResponse response = device.creditSale(amount)
                .withGratuity(new BigDecimal(0))
                .execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);

        Thread.sleep(15000);

        TerminalResponse refundResponse = device.creditRefund(amount)
                .withReferenceNumber(response.getTransactionId())
                .execute();

        assertNotNull(refundResponse);
        assertMitcTransactionResponse(refundResponse);
    }

    @Test
    public void CreditVerify() throws ApiException {
        TerminalResponse response = device.creditVerify().execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void CreditVoid() throws ApiException, InterruptedException {
        TerminalResponse response = device.creditSale(amount).execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);

        Thread.sleep(15000);

        TerminalResponse voidResponse = device.creditVoid()
                .withTransactionId(response.getTransactionId())
                .execute();

        assertNotNull(voidResponse);
        assertMitcTransactionResponse(voidResponse);
    }

    @Test
    public void CreditVoid_WithTerminalRefNumber() throws ApiException, InterruptedException {
        TerminalResponse response = device.creditSale(amount)
                .withGratuity(new BigDecimal(0))
                .execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);

        Thread.sleep(15000);

        TerminalResponse voidResponse = device.creditVoid()
                .withTerminalRefNumber(response.getTerminalRefNumber())
                .execute();

        assertNotNull(voidResponse);
        assertMitcTransactionResponse(voidResponse);
    }

    @Test
    public void EbtPurchase() throws ApiException {
        TerminalResponse response = device.ebtPurchase(amount)
                .execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void EbtPurchase_WithTip() throws ApiException {
        TerminalResponse response = device.ebtPurchase(amount)
                .withGratuity(new BigDecimal(0))
                .execute();

        assertNotNull(response);
        assertMitcTransactionResponse(response);
    }

    @Test
    public void EndOfDay() throws ApiException {
        IEODResponse response = device.endOfDay();

        assertNotNull(response);
        assertEquals("COMPLETE", response.getStatus());
        assertEquals("COMPLETE", response.getDeviceResponseText());
        assertEquals("00", response.getDeviceResponseCode());
        assertNotNull(response.getBatchId());
    }

    @Test
    public void CreditSale_WithoutAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            device.creditSale().execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditAuth_WithoutAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            device.creditAuth().execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCapture_WithoutTerminalRefNumber() throws ApiException {
        boolean exceptionCaught = false;
        try {
            device.creditCapture().execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("terminalRefNumber cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditRefund_WithoutAmount() throws ApiException {
        boolean exceptionCaught = false;
        try {
            device.creditRefund().execute();
        } catch (BuilderException ex) {
            exceptionCaught = true;
            assertEquals("amount cannot be null for this transaction type.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    private void assertMitcTransactionResponse(TerminalResponse response) {
        assertEquals("COMPLETE", response.getStatus());
        assertEquals("COMPLETE", response.getDeviceResponseText());
        assertEquals("00", response.getDeviceResponseCode());
        assertEquals("00", response.getResponseCode());
    }
}

