package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.enums.CardDataInputCapability;
import com.global.api.network.enums.CardHolderAuthenticationCapability;
import com.global.api.network.enums.CardHolderAuthenticationEntity;
import com.global.api.network.enums.TerminalOutputCapability;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.GiftCard;
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
public class VapsMiscTests {
    public VapsMiscTests() throws ApiException {
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
        config.setMerchantType("5542");

        ServicesContainer.configureService(config);
    }

    @Test
    public void creditPreAuthReversal() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        try {
            track.authorize(new BigDecimal(1), true)
                    .withCurrency("USD")
                    .withForceGatewayTimeout(true)
                    .execute();
            fail("Transaction did not timeout");
        }
        catch(GatewayTimeoutException exc) {
            assertEquals("1100", exc.getMessageTypeIndicator());
            assertEquals("003000", exc.getProcessingCode());
            assertEquals(0, exc.getReversalCount());
            assertNull(exc.getReversalResponseCode());
            assertNull(exc.getReversalResponseText());
        }
    }

    @Test
    public void storedValue_DE17() throws ApiException {
        GiftCard gift = TestCards.ValueLinkSwipe();

        Transaction response = gift.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withUniqueDeviceId("0201")
                .execute();
        assertNotNull(response);
        //assertEquals("000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(capture);
    }

    @Test
    public void preAuthCompletion_Reversal() throws ApiException {
        CreditTrackData track = TestCards.VisaSwipe();

        try {
            Transaction response = track.authorize(new BigDecimal(1), true)
                    .withCurrency("USD")
                    .execute();
            assertNotNull(response);
            assertEquals("000", response.getResponseCode());

            response.capture(new BigDecimal(10))
                    .withCurrency("USD")
                    .withForceGatewayTimeout(true)
                    .execute();
        }
        catch(GatewayTimeoutException exc) {
            assertEquals("1220", exc.getMessageTypeIndicator());
            assertEquals("003000", exc.getProcessingCode());
            assertEquals(0, exc.getReversalCount());
            assertNull(exc.getReversalResponseCode());
            assertNull(exc.getReversalResponseText());
        }
    }

    @Test
    public void onlyOneExceptionForTimeOutReversal() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        try {
            track.charge(new BigDecimal(15))
                    .withCurrency("USD")
                    .withForceGatewayTimeout(true)
                    .execute();
            fail("No timeout received.");
        }
        catch (GatewayTimeoutException exc) {
            assertNotNull(exc);
            assertEquals(exc.getReversalCount(), 3);
        }
    }
}
