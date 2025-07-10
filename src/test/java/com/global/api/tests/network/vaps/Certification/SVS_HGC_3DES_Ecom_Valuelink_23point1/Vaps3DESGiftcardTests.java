package com.global.api.tests.network.vaps.certification.SVS_HGC_3DES_Ecom_Valuelink_23point1;

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
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Vaps3DESGiftcardTests {
    private GiftCard valueLink;
    private GiftCard heartlandGiftCardSwipe;
    private GiftCard svs;
    private AcceptorConfig acceptorConfig ;
    private NetworkGatewayConfig config ;

    public Vaps3DESGiftcardTests() throws ApiException{
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
        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        //Used for HGC and SVS card testing
        config.setCompanyId("0009");
        config.setTerminalId("0001237891101");
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
        valueLink = new GiftCard();
        valueLink.setEncryptionData(EncryptionData.setKtbAndKsn("B1ADDC4F8C73F54A6F774ACD6C9DB09DD1E7EAEDB044711EAA08DBC78887256B8CD1D14DB7C2CC8D",
                "F000016F870850EB"));
        valueLink.setEncryptedPan("6B246DF0FF1EE73152F148C5F0C992CC");
        valueLink.setTrackNumber(TrackNumber.TrackTwo);
        valueLink.setEntryMethod(EntryMethod.Swipe);
        valueLink.setExpiry("2501");
        valueLink.setCardType("ValueLink");

        // Heartland Giftcard
        heartlandGiftCardSwipe = new GiftCard();
        heartlandGiftCardSwipe.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        heartlandGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        heartlandGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        heartlandGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        heartlandGiftCardSwipe.setExpiry("2501");
        heartlandGiftCardSwipe.setCardType("HeartlandGift");

        heartlandGiftCardSwipe = TestCards.HeartlandGiftCardSwipe();
        svs = new GiftCard();
        svs.setEncryptionData(EncryptionData.setKSNAndEncryptedData("EC7EB2F7BD67A2784F1AD9270EFFD90DD121B8653623911C6BC7B427F726A49F834CA051A6C1CC9CBB17910A1DBA209796BB6D08B8C374A2912AB018A679FA5A0A0EDEADF349FED3",
                "F000019990E00003"));
        svs.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        svs.setTrackNumber(TrackNumber.TrackTwo);
        svs.setEntryMethod(EntryMethod.Swipe);
        svs.setExpiry("2512");
        svs.setCardType("StoredValue");
    }

    @Test
    public void test_valueLink_activate() throws ApiException {

        Transaction response = valueLink.activate(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_valueLink_balance_inquiry() throws ApiException {
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
    public void test_04_valueLink_auth() throws ApiException {
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
    public void test_09_valueLink_purchase() throws ApiException {
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
    public void test_05_valueLink_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = valueLink.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        response.setNtsData(ntsData);

        Transaction reverseResponse = response.reverse().execute("ValueLink");
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }
    @Test
    public void test_14_valueLink_return() throws ApiException {
        Transaction response = valueLink.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_14_valueLink_auth_capture() throws ApiException {
        Transaction response = valueLink.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);

        Transaction captureResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(captureResponse);

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_01_activate_SVS() throws ApiException {
        Transaction response = svs.activate(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("900060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_02_balance_inquiry_SVS() throws ApiException {
        Transaction response = svs.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_04_auth_SVS() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = svs.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ICR");

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_09_purchase_SVS() throws ApiException {

        GiftCard svs = new GiftCard();
        svs.setValue(";6006491260550251190=711211057329556?");
        Transaction response = svs.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_005_refund_SVS() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized),
                svs
        );

        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"12123")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_01_heartlandGiftCard_activate() throws ApiException {

        Transaction response = heartlandGiftCardSwipe.activate(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("900060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_balance_inquiry_HGC() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.balanceInquiry()
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("316000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_04_heartlandGiftCard_auth_HGC() throws ApiException {
        heartlandGiftCardSwipe.setEncryptionData(EncryptionData.setKtbAndKsn("3A2067D00508DBE43E3342CC77B0575E04D9191B380C88036DD82D54C834DCB4",
                "F000019990E00003"));
        heartlandGiftCardSwipe.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        heartlandGiftCardSwipe.setTrackNumber(TrackNumber.TrackTwo);
        heartlandGiftCardSwipe.setEntryMethod(EntryMethod.Swipe);
        heartlandGiftCardSwipe.setExpiry("2501");
        heartlandGiftCardSwipe.setCardType("HeartlandGift");

        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_05_heartlandGiftCard_purchase() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_13_giftCard_return_HGC() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(),
                heartlandGiftCardSwipe
        );

        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withClerkId("41256")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"062517")
                .execute();
        response.setNtsData(new NtsData());
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200",pmi.getMessageTransactionIndicator());
        assertEquals("200060",pmi.getProcessingCode());
        assertEquals("200",pmi.getFunctionCode());

        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
}
