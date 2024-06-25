package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.NetworkGatewayType;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class NwsGiftTests {
    private GiftCard giftCard;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;

    public NwsGiftTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();

        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.None);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.TwelveCharacters);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        //DE 127
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        acceptorConfig.setTokenizationType(TokenizationType.MerchantTokenization);
        acceptorConfig.setMerchantId("650000011573667");

        //DE 127
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setUniqueDeviceId("0001");

        ServicesContainer.configureService(config);

        config.setMerchantType("5542");
        //ServicesContainer.configureService(config, "ICR");

        config.setNodeIdentification("VLK2");
        //ServicesContainer.configureService(config, "ValueLink");

        // VALUE LINK
        //giftCard = TestCards.ValueLinkManual();
        //giftCard = TestCards.ValueLinkSwipe();

        // SVS
        giftCard = TestCards.SvsManual();
        giftCard = TestCards.SvsSwipe();

        // HMS
//        giftCard = TestCards.HMSManual();
//        giftCard = TestCards.HMSSwipe();

        //GIFT CARD
//        giftCard = TestCards.GiftCard2Manual();
//       giftCard = TestCards.GiftCard1Swipe();
    }

    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    //Input Mode 6 - Key Entry (Manual Entry)
    //182. Run a MTI 1200 Sale transaction
    //Input Mode 2 – Magnetic Stripe Read DE 45 Track 1 or DE 35 Track 2 Data (Inside)
    //183. Run a MTI 1100 Open to Buy/Balance Inquiry transaction
    //184. Run a MTI 1100 Authorization transaction
    //185. Run a MTI 1200 Sale transaction
    //186. Run a MTI 1200 Return transaction
    //187. Run a MTI 1200 Replenish/Reload transaction
    //188. Run a MTI 1200 Cash out transaction
    //189. Run a MTI 1200 Activation transaction
    //190. Run a MTI 1220 Voice-capture transaction
    //191. Run a MTI 1420 Void transaction
    //192. Create a MTI 1420 Reversal, reversing a Sale transaction
    //193. Create a MTI 1420 Reversal, reversing a Return transaction
    //194. Run a Batch Detail Report to verify Reconciliation Accumulators.

    @Test
    public void giftCard_activate() throws ApiException {
        Transaction response = giftCard.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(25))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_authorize() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10.00))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_cash_out() throws ApiException {
        Transaction response = giftCard.cashOut()
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_auth_capture() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(50), true)
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                response.getAuthorizationCode(),
                response.getNtsData(),
                giftCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime()
        );

        Transaction captureResponse = transaction.capture(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(captureResponse);
        assertEquals("000", captureResponse.getResponseCode());
    }
    @Test
    public void giftCard_voice_capture() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                NtsData.voiceAuthorized(),
                giftCard
        );

        Transaction response = trans.capture()
                .withReferenceNumber("12345")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
  
    @Test
    public void giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    //Not working
    @Test
    public void giftCard_sale_cashBack() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(41.00))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(40.00))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_return() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(12))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_void() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(25.00))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }

        @Test
    public void giftCard_reversal() throws ApiException {
        try{
            giftCard.charge(new BigDecimal(11.00))
                    .withCurrency("USD")
                    .execute();
           // fail("No exception thrown");
        }
        catch(GatewayTimeoutException exc) {

        }
    }
    @Test
    public void giftCard_reload() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_recharge() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }


//    @Test
//    public void giftCard_reversal() throws ApiException {
//        try{
//            giftCard.charge(new BigDecimal(11.00))
//                    .withCurrency("USD")
////                    .withForceGatewayTimeout(true)
//                    .execute();
//           // fail("No exception thrown");
//        }
//        catch(GatewayTimeoutException exc) {
//
//        }
//    }


    @Test
    public void value_link_card_type_test() throws ApiException {
        GiftCard card = new GiftCard();
        card.setValue("6010567085878703=25010004000070779628");

        Transaction response = card.authorize(new BigDecimal(1.00), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_giftCard_void() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                response.getAuthorizationCode(),
                ntsData,
                giftCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime()
        );


        Transaction voidResponse = response
                .voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void test001_giftCard_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(reversal);

        // check message data
        PriorMessageInformation pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("400", reversal.getResponseCode());

    }

    //For code coverage
    @Test
    public void giftCard_authorize01() throws ApiException {
        giftCard = TestCards.GiftCard2Manual();
        ServicesContainer.configureService(config);
        Transaction response = giftCard.authorize(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_authorize02() throws ApiException {
       giftCard = TestCards.SvsManual();
        ServicesContainer.configureService(config);
        Transaction response = giftCard.authorize(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

}
