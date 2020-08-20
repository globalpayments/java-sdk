package com.global.api.tests.network;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayComsException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.gateways.events.IGatewayEvent;
import com.global.api.gateways.events.IGatewayEventHandler;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.CardHolderAuthenticationEntity;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NetworkSimulatedErrorTests {
    private CreditCardData card;

    public NetworkSimulatedErrorTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setGatewayEventHandler(new IGatewayEventHandler() {
            public void eventRaised(IGatewayEvent event) {
                System.out.println(event.getEventMessage());
            }
        });

        ServicesContainer.configureService(config);

        config.setPrimaryEndpoint("test1.txns-c.secureexchange.net");
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        ServicesContainer.configureService(config, "bad-primary");

        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setSecondaryEndpoint("test1.txns-e.secureexchange.net");
        ServicesContainer.configureService(config, "bad-secondary");

        // VISA
        card = TestCards.VisaManual(true, true);
    }

    @Test
    public void test_000_simulateTimeoutOnPrimary() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        assertNotNull(response.getMessageInformation());
        assertEquals(Host.Secondary, response.getMessageInformation().getProcessingHost());
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_001_simulateTimeoutOnSecondary() throws ApiException {
        card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Connection)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_002_simulateTimeoutOnBothHosts() throws ApiException {
        card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
    }

    @Test
    public void test_003_simulateConnectionFailureOnPrimary() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Connection)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        assertNotNull(response.getMessageInformation());
        assertEquals(Host.Secondary, response.getMessageInformation().getProcessingHost());
    }

    @Test(expected = GatewayComsException.class)
    public void test_004_simulateConnectionFailureOnSecondary() throws ApiException {
        card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Connection)
                .withSimulatedHostErrors(Host.Secondary, HostError.Connection)
                .execute();
    }

    @Test
    public void test_005_simulateSendFailureOnPrimary() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.SendFailure)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        assertNotNull(response.getMessageInformation());
        assertEquals(Host.Secondary, response.getMessageInformation().getProcessingHost());
    }

    @Test(expected = GatewayComsException.class)
    public void test_006_simulateSendFailureOnSecondary() throws ApiException {
        card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Connection)
                .withSimulatedHostErrors(Host.Secondary, HostError.SendFailure)
                .execute();
    }

    @Test(expected = GatewayTimeoutException.class)
    public void test_006_simulateTimeoutOnPrimaryConnectionOnSecondary() throws ApiException {
        card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Connection)
                .execute();
    }

    @Test
    public void test_007_bad_primary() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute("bad-primary");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        assertNotNull(response.getMessageInformation());
        assertEquals(Host.Secondary, response.getMessageInformation().getProcessingHost());
    }

    @Test(expected = GatewayComsException.class)
    public void test_008_bad_secondary() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Connection)
                .execute("bad-secondary");
        assertNotNull(response);
    }
}
