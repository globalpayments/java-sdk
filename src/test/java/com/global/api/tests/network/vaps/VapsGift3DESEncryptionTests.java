package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class VapsGift3DESEncryptionTests {
    private GiftCard giftCard;
    private AcceptorConfig acceptorConfig;

    public VapsGift3DESEncryptionTests() throws ApiException {
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
        acceptorConfig.setHardwareLevel("S3");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");

        ServicesContainer.configureService(config);

        //Gift card
        config.setNodeIdentification("VLK2");
        ServicesContainer.configureService(config, "ValueLink");

        // VALUE LINK
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F0000100095E6701"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setExpiry("2501");
        giftCard.setCardType("ValueLink");

    }
    //-----------------------------------------------GiftCard----------------------------------------

    @Test
    public void test_001_GiftCard_auth() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10),true)
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
    public void test_003_giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
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
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute("ValueLink");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("210060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_GiftCard_preAuthCompletion() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("100",pmi.getFunctionCode());
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(captureResponse);
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("201",pmi.getFunctionCode());
        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void giftCard_return() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(10.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute("ValueLink");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("200060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_004_giftCard_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized);

        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // void the transaction test case #8
        Transaction reverseResponse = response.reverse().execute("ValueLink");
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }
    @Test
    public void test_GiftCard_auth_capture() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // test_019
        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_GiftCard_swipe_voice_capture() throws ApiException {
        Transaction preResponse = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(preResponse);
        assertEquals(preResponse.getResponseMessage(), "000", preResponse.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10), preResponse.getAuthorizationCode(),
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                giftCard
        );

        Transaction response = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("006000", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_GiftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
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
    public void value_link_Replenish_void() throws ApiException {

        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute("ValueLink");
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_005_GiftCard_void_return() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute("ValueLink");
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_007_giftCard_refund_capture() throws ApiException {

        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction capture = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(capture);
        assertEquals(capture.getResponseMessage(), "000", capture.getResponseCode());
    }

    @Test //Negative scenerio card detail without card name
    public void test_GiftCard_auth() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertTrue(("904").matches(response.getResponseCode()));
    }
}