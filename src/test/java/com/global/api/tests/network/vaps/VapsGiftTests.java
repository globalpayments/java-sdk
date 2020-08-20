package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Host;
import com.global.api.entities.enums.HostError;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.enums.*;
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

    public VapsGiftTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);

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

        ServicesContainer.configureService(config);

        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");

        config.setNodeIdentification("VLK2");
        ServicesContainer.configureService(config, "ValueLink");

        // VALUE LINK
        //giftCard = TestCards.ValueLinkManual();

        // SVS
        giftCard = TestCards.SvsManual();
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
    }

    @Test
    public void giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(25.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_sale() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute("ValueLink");
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
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
    public void giftCard_sale_cashBack() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(41.00))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(40.00))
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

        Transaction captureResponse = transaction.capture(new BigDecimal(35.24))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());
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
        Transaction response = giftCard.refund(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_void() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("000", voidResponse.getResponseCode());
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

        Transaction response = card.authorize(new BigDecimal(1.00), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
}
