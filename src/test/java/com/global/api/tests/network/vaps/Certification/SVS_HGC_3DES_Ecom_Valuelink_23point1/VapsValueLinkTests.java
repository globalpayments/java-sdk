package com.global.api.tests.network.vaps.certification.SVS_HGC_3DES_Ecom_Valuelink_23point1;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsValueLinkTests {
    private GiftCard valueLink;
    private AcceptorConfig acceptorConfig ;
    private NetworkGatewayConfig config ;

    public VapsValueLinkTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("S1");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);

        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0009");
        config.setTerminalId("0001237891101");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        valueLink = new GiftCard();
        valueLink.setValue(";6010567093430380=25010004000070779074?");
        valueLink.setCardType("ValueLink");

        config.setNodeIdentification("VLK2");
        ServicesContainer.configureService(config, "ValueLink");
    }

    @Test
    public void test_01_valueLink_activate() throws ApiException {

        Transaction response = valueLink.activate(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_06_valueLink_balance_inquiry() throws ApiException {
        Transaction response = valueLink.balanceInquiry()
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_07_valueLink_auth() throws ApiException {
        Transaction response = valueLink.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ValueLink");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_09_valueLink_sale() throws ApiException {
        Transaction response = valueLink.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_08_valueLink_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);

        Transaction response = valueLink.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        response.setNtsData(ntsData);
        Transaction reverseResponse = response.reverse()
                .execute("ValueLink");
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void test_10_valueLink_return() throws ApiException {
        Transaction response = valueLink.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_value_link_Replenish_void() throws ApiException {

        Transaction response = valueLink.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_11_valueLink_auth_capture() throws ApiException {
        Transaction response = valueLink.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());
    }
}
