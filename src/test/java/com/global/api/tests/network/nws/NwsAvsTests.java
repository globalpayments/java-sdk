package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NetworkGatewayType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.CardHolderAuthenticationEntity;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsAvsTests {
    public NwsAvsTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
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
        NetworkGatewayConfig config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA03");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    //001. Run a American Express MTI 1100 Authorization
    @Test
    public void test_001_amex_auth() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90071"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //002. Run a Discover MTI 1100 Authorization
    @Test
    public void test_002_discover_auth() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90050"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //007. Run a American Express MTI 1200 Sale
    @Test
    public void test_007_amex_sale() throws ApiException {
        CreditCardData card = TestCards.AmexManual(true, true);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90076"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //008. Run a Discover MTI 1200 Sale
    @Test
    public void test_008_discover_sale() throws ApiException {
        CreditCardData card = TestCards.DiscoverManual(true, true);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90050"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //009. Run a MasterCard MTI 1200 Sale
    @Test
    public void test_009_mastercard_sale() throws ApiException {
        CreditCardData card = TestCards.MasterCardManual(true, true);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90038"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //010. Run a Visa MTI 1200 Sale
    @Test
    public void test_010_visa_sale() throws ApiException {
        CreditCardData card = TestCards.VisaManual(true, true);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("10000"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //011. Run a MasterCard Fleet MTI 1200 Sale
    @Test
    public void test_011_mastercard_fleet_sale() throws ApiException {
        CreditCardData card = TestCards.MasterCardFleetManual(true, true);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90038"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //012. Run a Visa Fleet MTI 1200 Sale
    @Test
    public void test_012_visa_fleet_sale() throws ApiException {
        CreditCardData card = TestCards.VisaFleetManual(true, true);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("100000"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //    Input Mode 2 – Magnetic Stripe Read DE 45 Track 1 or DE 35 Track 2 Data (Inside)
    //019. Run a American Express MTI 1200 Sale
    @Test
    public void test_019_amex_sale() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90085"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //020. Run a Discover MTI 1200 Sale
    @Test
    public void test_020_discover_sale() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90058"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //021. Run a MasterCard MTI 1200 Sale
    @Test
    public void test_021_mastercard_sale() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90039"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //022. Run a Visa MTI 1200 Sale
    @Test
    public void test_022_visa_sale() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90018"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //023. Run a MasterCard Fleet MTI 1200 Sale
    @Test
    public void test_023_mastercard_fleet_sale() throws ApiException {
        CreditTrackData card = TestCards.MasterCardFleetSwipe();

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90038"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //024. Run a Visa Fleet MTI 1200 Sale
    @Test
    public void test_024_visa_fleet_sale() throws ApiException {
        CreditTrackData card = TestCards.VisaFleetSwipe();

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("10000"))
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //    Input Mode 2 – Magnetic Stripe Read DE 45 Track 1 or DE 35 Track 2 Data (ICR)
    //025. Run a American Express MTI 1100 Authorization
    @Test
    public void test_025_amex_auth() throws ApiException {
        CreditTrackData card = TestCards.AmexSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90077"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //026. Run a Discover Card MTI 1100 Authorization
    @Test
    public void test_026_discover_auth() throws ApiException {
        CreditTrackData card = TestCards.DiscoverSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90053"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //027. Run a MasterCard MTI 1100 Authorization
    @Test
    public void test_027_mastercard_auth() throws ApiException {
        CreditTrackData card = TestCards.MasterCardSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90034"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //028. Run a Visa MTI 1100 Authorization
    @Test
    public void test_028_visa_auth() throws ApiException {
        CreditTrackData card = TestCards.VisaSwipe();

        Transaction response = card.authorize(new BigDecimal(0))
                .withCurrency("USD")
                .withAddress(new Address("90009"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //029. Run a MasterCard Fleet MTI 1100 Authorization
    @Test
    public void test_029_mastercard_fleet_auth() throws ApiException {
        CreditTrackData card = TestCards.MasterCardFleetSwipe();

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("90038"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //030. Run a Visa Fleet MTI 1100 Authorization
    @Test
    public void test_030_visa_fleet_auth() throws ApiException {
        CreditTrackData card = TestCards.VisaFleetSwipe();

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withAddress(new Address("10000"))
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
}
