package com.global.api.tests.network;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

public class NetworkValidationTests {
    @Test(expected = ConfigurationException.class)
    public void config_NoPrimaryEndpoint() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_NoPrimaryPort() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_NoSecondaryPort() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setSecondaryEndpoint("test.txns-c.secureexchange.net");
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_NoCompanyId() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_NoTerminalId() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_AcceptorConfig_HardwareLevel_OverLength() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setHardwareLevel("12345");

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_AcceptorConfig_SoftwareLevel_OverLength() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setSoftwareLevel("1234567");

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = ConfigurationException.class)
    public void config_AcceptorConfig_OSLevel_OverLength() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setSoftwareLevel("1234567");

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);
    }

    @Test(expected = BuilderException.class)
    public void batchClose_NoProvider() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        BatchService.closeBatch();
    }

    @Test(expected = BuilderException.class)
    public void batchClose_NoDebitTotal() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        BatchService.closeBatch(0, new BigDecimal(0), null);
    }

    @Test(expected = BuilderException.class)
    public void batchClose_NoCreditTotal() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();
        // acceptor required fields

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        BatchService.closeBatch(0, null, new BigDecimal(0));
    }

    @Test(expected = BuilderException.class)
    public void reversal_NoProcessingCode() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        Transaction reference = Transaction.fromNetwork(
            new BigDecimal(10),
            "TYPE04",
            new NtsData(),
            TestCards.VisaSwipe()
        );

        reference.reverse().execute();
    }

    @Test(expected = BuilderException.class)
    public void wex_refund_noTransactionMatching() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        card.refund(new BigDecimal(10))
            .withCurrency("USD")
            .execute();
    }

    @Test(expected = BuilderException.class)
    public void wex_refund_transactionMatching_nullBatch() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactionMatchingData(new TransactionMatchingData(null, "0114"))
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void wex_refund_transactionMatching_emptyBatch() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactionMatchingData(new TransactionMatchingData("", "0114"))
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void wex_refund_transactionMatching_nullDate() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactionMatchingData(new TransactionMatchingData("0000040067", null))
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void wex_refund_transactionMatching_emptyDate() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(12345);
        config.setAcceptorConfig(acceptorConfig);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");

        ServicesContainer.configureService(config);

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=19020003069200000");

        card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withTransactionMatchingData(new TransactionMatchingData("0000040067", ""))
                .execute();
    }

    @Test(expected = BuilderException.class)
    public void readyLink_loadReversal_wrong_cardType() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4111111111111111");
        card.setExpMonth(12);
        card.setExpYear(2025);

        card.loadReversal(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
    }
}