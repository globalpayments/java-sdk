package com.global.api.tests.transit;

import com.global.api.entities.enums.Environment;
import com.global.api.entities.enums.GatewayProvider;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.TransitConfig;
import com.global.api.services.TransitService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransitAuthenticationTests {

    public TransitAuthenticationTests() throws ApiException {
        //ServicesContainer.configureService(getConfig());
    }

    @Test
    public void generateKeyManual() throws ApiException {
        String transactionKey = TransitService.generateTransactionKey(
                Environment.TEST,
                "884000003531",
                "TA5876503",
                "HRQATest!000"
        );
        assertNotNull(transactionKey);
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
}