package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.logging.INetworkRequestLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify Portico logging behavior:
 * - Default console logger (setEnableLogging only)
 * - Custom logger callback behavior (enableLogging=false)
 */
public class PorticoLoggingTests {

    private static final String SECRET_API_KEY = "skapi_cert_MTeSAQAfG1UA9qQDrzl-kz4toXvARyieptFwSKP24w";
    private static final String SERVICE_URL   = "https://cert.api2.heartlandportico.com";

    private CreditCardData card;

    // Test-only logger mock for request/response callback assertions.
    private static class MyNetworkRequestLogger implements INetworkRequestLogger {
        private final List<String> requests = new ArrayList<>();
        private final List<String> responses = new ArrayList<>();

        @Override
        public void logInfo(String message) {
            // no-op for this test
        }
        @Override
        public void logMessage(String message, String value) {
            // no-op for this test
        }
        @Override
        public <E extends Enum<E>> void logMessage(String fieldName, E value) {
            // no-op for this test
        }

        @Override
        public void RequestSent(String request) {
            requests.add(request);
        }

        @Override
        public void ResponseReceived(String response) {
            responses.add(response);
        }

        public List<String> getRequests() {
            return requests;
        }

        public List<String> getResponses() {
            return responses;
        }
    }

    @BeforeEach
    public void setup() {
        card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2029);
        card.setCvn("123");
    }

    /**
     * Test 1: When only setEnableLogging(true) is set with no explicit requestLogger,
     * the default RequestConsoleLogger should automatically log the full SOAP request
     * and gateway response to stdout in the standard formatted layout.
     */
    @Test
    public void Logging_WhenEnabled_DefaultConsoleLogger_PrintsRequestAndResponse() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey(SECRET_API_KEY);
        config.setServiceUrl(SERVICE_URL);
        config.setEnableLogging(true); // no explicit requestLogger — uses Gateway's default

        ServicesContainer.configureService(config);

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(captured));

        try {
            Transaction response = card.charge(new BigDecimal("10.00"))
                    .withCurrency("USD")
                    .withAllowDuplicates(true)
                    .execute();
            assertNotNull(response);
            assertEquals("00", response.getResponseCode());
        } finally {
            System.setOut(original);
        }

        String output = captured.toString();
        assertTrue(output.contains("Endpoint:"),    "Should log the endpoint URL");
        assertTrue(output.contains("Request Body:"), "Should log the request body section");
        assertTrue(output.contains("PosRequest"),   "Request should contain SOAP PosRequest element");
        assertTrue(output.contains("GatewayRspCode"), "Response should contain GatewayRspCode");
        assertTrue(output.contains("PosResponse"),  "Response should contain PosResponse element");
    }

    /**
     * Test 2: With enableLogging(false) and a custom IRequestLogger, the custom logger should still
     * receive the full SOAP request and gateway XML response callbacks.
     */
    @Test
    public void Logging_WhenDisabled_CustomLogger_RequestAndResponseMethodsAreCalled() throws ApiException {
        MyNetworkRequestLogger customLogger = new MyNetworkRequestLogger();

        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey(SECRET_API_KEY);
        config.setServiceUrl(SERVICE_URL);
        config.setEnableLogging(false);
        config.setRequestLogger(customLogger);

        ServicesContainer.configureService(config);

        Transaction response = card.charge(new BigDecimal("10.00"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // Verify RequestSent was called with the outgoing SOAP XML
        assertFalse(customLogger.getRequests().isEmpty(),  "RequestSent should have been called");
        assertTrue(customLogger.getRequests().get(0).contains("PosRequest"),  "Request must contain PosRequest element");
        assertTrue(customLogger.getRequests().get(0).contains("CreditSale"),  "Request must contain the transaction type");

        // Verify ResponseReceived was called with the gateway XML response
        assertFalse(customLogger.getResponses().isEmpty(), "ResponseReceived should have been called");
        assertTrue(customLogger.getResponses().get(0).contains("PosResponse"),   "Response must contain PosResponse element");
        assertTrue(customLogger.getResponses().get(0).contains("GatewayRspCode"), "Response must contain GatewayRspCode");
        assertTrue(customLogger.getResponses().get(0).contains("GatewayTxnId"),  "Response must contain a GatewayTxnId");
    }
    
}

