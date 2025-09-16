package com.global.api.tests.transit;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.TransitConnector;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.TransitConfig;
import com.global.api.tests.testdata.TestCards;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransitAdminTests {
    private final CreditCardData card;

    public TransitAdminTests() throws ApiException {
        card = TestCards.VisaManual(false, false);

        ServicesContainer.configureService(getConfig());
    }

    protected TransitConfig getConfig() {
        TransitConfig config = new TransitConfig();
        config.setMerchantId("884000003531");
        config.setUsername("TA5876503");
        config.setPassword("HRQATest!000");
        config.setDeviceId("88400000353102");
        config.setTransactionKey("7WDYEC6LE9T5Q8EER5CWRPN3P4O5BZH8");
        config.setDeveloperId("003226G001");
        config.setGatewayProvider(GatewayProvider.TRANSIT);
        config.setAcceptorConfig(new AcceptorConfig());
        return config;
    }

    @Test
    public void testTokenizeCardKeyed() throws ApiException {
        String token = card.tokenize();
        assertNotNull(token);
    }

    @Test
    public void testCreateManifest() throws ApiException {
        TransitConfig config = getConfig();

        ServicesContainer.configureService(config);
        TransitConnector provider = (TransitConnector) ServicesContainer.getInstance().getGateway("default");

        // create Transaction Key
        String key = provider.getTransactionKey();

        assertNotNull(key);

        // create Manifest
        provider.setTransactionKey(key);
        String manifest = provider.createManifest();

        assertNotNull(manifest);
    }

    @Test
    public void testDisableTransactionKey() throws ApiException {
        TransitConfig config = getConfig();
        // TransactionKey needs to be disabled. Throw 'Invalid Transaction Key' when key is not in active state
        config.setTransactionKey("F508Z7TIGFORSTDYJQLMK9NGFFPBIXV0");
        config.setAcceptorConfig(new AcceptorConfig());
        config.setGatewayProvider(GatewayProvider.TRANSIT);

        ServicesContainer.configureService(config);
        TransitConnector provider = (TransitConnector) ServicesContainer.getInstance().getGateway("default");

        // create new Transaction Key
        String key = provider.getTransactionKey();

        assertNotNull(key);

    }
}