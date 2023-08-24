package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NWSReadyLinkTests {
    private CreditCardData card;
    private CreditTrackData track;
    private NetworkGatewayConfig config;

    public NWSReadyLinkTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

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
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }
    @Test
    public void test_027_readyLink_load_declined_15_dollar_transaction() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(15))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_027_readyLink_load() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(20))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_028_ready_link_reversal() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4000000000000001=07121548607334555");
        track.setCardType("VisaReadyLink");
        Transaction response = null;

        response = track.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Host_Authorized);
        response.setNtsData(ntsData);

        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        assertEquals("000", reversal.getResponseCode());

    }
    @Test
    public void test_027_readyLink_load_with_fee_amount() throws ApiException {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";4009081122223335=21121010000012345678?");
        track.setCardType("VisaReadyLink");

        Transaction response = track.addValue(new BigDecimal(250))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("600008", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

}
