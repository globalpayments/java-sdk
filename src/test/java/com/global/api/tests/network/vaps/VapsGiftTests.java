package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.elements.DE22_PosDataCode;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
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

public class VapsGiftTests {
    private GiftCard giftCard;
    private GiftCard heartlandGiftCardSwipe;
    private NetworkGatewayConfig config;
    private AcceptorConfig acceptorConfig;

    public VapsGiftTests() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Attended);


        // hardware software config values
        acceptorConfig.setHardwareLevel("S3");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

        // gateway config
        config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0009");
        config.setTerminalId("0001237891001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

//        config.setNodeIdentification("VLK2");
//        ServicesContainer.configureService(config, "ValueLink");

        // VALUE LINK
        //giftCard = TestCards.ValueLinkManual();

        // SVS
        giftCard = TestCards.SvsSwipe();
        heartlandGiftCardSwipe = TestCards.HeartlandGiftCardSwipe();
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
        Transaction response = giftCard.activate(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        String irr = response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        NtsData ntsData = new NtsData();
        response.setNtsData(ntsData);
        Transaction captureResponse = response.capture(new BigDecimal(35.24))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,irr)
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());

    }

    @Test
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test // Authorize is an outside transaction
    public void giftCard_authorize() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(50), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void giftCard_reversal() throws ApiException {
        try{
            giftCard.charge(new BigDecimal(11.00))
                    .withCurrency("USD")
                    .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                    .execute();
            fail("No exception thrown");
        }
        catch(GatewayTimeoutException exc) {

        }
    }

    @Test
    public void test_023_sale_reversal() throws ApiException {

        Transaction response = giftCard.charge(new BigDecimal(6))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
        NtsData ntsData = new NtsData();
        response.setNtsData(ntsData);

        Transaction reversal = response.reverse()
               .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
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
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = giftCard.authorize(new BigDecimal(50),true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                response.getAuthorizationCode(),
                response.getNtsData(),
                giftCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime()
        );
        String irr = response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);

        Transaction captureResponse = transaction.capture(new BigDecimal(35.24))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,irr)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());
    }

    @Test
    public void test_GiftCard_preAuthCompletion() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
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
    public void giftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_return() throws ApiException {
        Transaction response1 = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response1);

        assertEquals("000", response1.getResponseCode());

        NtsData ntsData = new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized);
        response1.setNtsData(ntsData);

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                NtsData.interchangeAuthorized(),
                giftCard
        );
        Transaction response = trans.refund(new BigDecimal(5))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,response1.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
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
    public void value_link_card_type_test() throws ApiException {
        GiftCard card = new GiftCard();
        card.setValue("6010567085878703=25010004000070779628");

        Transaction response = giftCard.authorize(new BigDecimal(1.00), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void value_link_void() throws ApiException {
        GiftCard card = new GiftCard();
        card.setValue("6010567085878703=25010004000070779628");
        card.setCardType("ValueLink");

        Transaction response = card.addValue(new BigDecimal(10.00))
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
    public void heartland_giftCard_add_value() throws ApiException {

        giftCard = TestCards.GiftCard2Manual();
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

//    -----------------------------------Heartland GiftCards--------------------------------------------
    @Test
    public void giftCard_activate_HGC() throws ApiException {

        Transaction response = heartlandGiftCardSwipe.activate(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_sale_HGC() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void giftCard_balance_inquiry_HGC() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test // Authorize is an outside transaction
    public void heartlandGiftCard_authorize() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);

        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(50), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        String irr = response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        NtsData ntsData = new NtsData();

        response.setNtsData(ntsData);
        Transaction captureResponse = response.capture(new BigDecimal(35.24))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,irr)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());

    }

    @Test
    public void giftCard_return_HGC() throws ApiException {

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(),
                heartlandGiftCardSwipe
        );

        Transaction response = trans.refund(new BigDecimal(5))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"062517")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_reversible_HGC() throws ApiException {

        Transaction response = heartlandGiftCardSwipe.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                heartlandGiftCardSwipe,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }

    @Test
    public void HGC_balance_inquiry() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_09_purchase_SVS() throws ApiException {

        Transaction response = giftCard.charge(new BigDecimal(100))
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
    public void test_13_giftCard_return_svs() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(),
                giftCard
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
