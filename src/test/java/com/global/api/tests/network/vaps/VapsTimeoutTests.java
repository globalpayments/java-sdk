package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.gateways.events.IGatewayEventHandler;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VapsTimeoutTests {
    public VapsTimeoutTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setHardwareLevel("3750");
        acceptorConfig.setSoftwareLevel("04010031");
        acceptorConfig.setOperatingSystemLevel("Q50016A6");

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test2.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setEnableLogging(true);
        config.setGatewayEventHandler(new IGatewayEventHandler() {
            public void eventRaised(IGatewayEvent event) {
                System.out.print(event.getEventMessage() + "\r\n");
            }
        });

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_001_authorization_timeout() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue("4012002000060016=25121011803939600000");

        try {
            track.charge(new BigDecimal("10"))
                    .withCurrency("USD")
                    .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                    .execute();
        }
        catch(GatewayTimeoutException exc) {
            assertNotNull(exc.getTransactionToken());
        }
    }
}
