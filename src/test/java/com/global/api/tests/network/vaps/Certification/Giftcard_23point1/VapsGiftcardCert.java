package com.global.api.tests.network.vaps.Certification.Giftcard_23point1;

import com.global.api.ServicesContainer;
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
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsGiftcardCert {
    private GiftCard giftCard;
    private GiftCard heartlandGiftCardSwipe;
    private NetworkGatewayConfig config;
    private AcceptorConfig acceptorConfig;

    public VapsGiftcardCert() throws ApiException {
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

        // SVS
        giftCard = TestCards.SvsSwipe();

        //HGC
        heartlandGiftCardSwipe = TestCards.HeartlandGiftCardSwipe();
    }
    @Test
    public void test_cert_svs_giftCard_activate() throws ApiException {
        Transaction response = giftCard.activate(new BigDecimal(25))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_balance_inquiry() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_authorize() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(15),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_add_value() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(100.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_purchase() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(12.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_purchase_with_cashback() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(12.00))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(13))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_reversible() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        Transaction reversal = response.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_svs_giftCard_purchase_void() throws ApiException {

        Transaction response = giftCard.charge(new BigDecimal(13.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("000",voidResponse.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_purchaseReversal() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(11))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction voidResponse = response.reverse()
                .execute();
        assertNotNull(voidResponse);
        assertEquals("000",voidResponse.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_addValue_replenish() throws ApiException {
        Transaction response = giftCard.addValue(new BigDecimal(100.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_svs_giftCard_return() throws ApiException {
        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(),
                giftCard
        );

        Transaction response = trans.refund(new BigDecimal(5))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"062517")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

    }
    @Test
    public void test_cert_svs_giftCard_auth_capture() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = giftCard.authorize(BigDecimal.valueOf(10.5),true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(),
                giftCard,
                "1100",
                "000012",
                "250221073641"
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
    public void test_cert_svs_giftCard_cash_out() throws ApiException {
        Transaction response = giftCard.cashOut()
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_01_heartland_giftCard_activate() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.activate(new BigDecimal(100.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_02_heartlandGiftCard_balance_inquiry() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_04_heartlandGiftCard_authorize() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(25), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        String irr = response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        System.out.println(irr);
        NtsData ntsData = new NtsData();

        response.setNtsData(ntsData);
        Transaction captureResponse = response.capture(new BigDecimal(15.45))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,irr)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());

    }
    @Test
    public void test_cert_05_heartlandGiftCard_reversible() throws ApiException {
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

        Transaction reversal = transaction.voidTransaction()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_06_heartlandGiftCard_void() throws ApiException {

        Transaction response = heartlandGiftCardSwipe.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
        System.out.println(response.getAuthorizationCode());
        System.out.println(response.getMessageTypeIndicator());
        System.out.println(response.getSystemTraceAuditNumber());
        System.out.println(response.getOriginalTransactionTime());
        System.out.println(response.getProcessingCode());

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

        Transaction voidTransaction = transaction.voidTransaction(new BigDecimal(10.00))
                .execute();
        assertNotNull(voidTransaction);
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
        assertEquals(voidTransaction.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_07_heartlandGiftCard_preAuthCompletion() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ICR");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .execute("ICR");
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
    public void test_cert_08_heartlandGiftCard_preAuthReverse() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");
        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute("ICR");
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100",pmi.getMessageTransactionIndicator());
        assertEquals("006000",pmi.getProcessingCode());
        assertEquals("101",pmi.getFunctionCode());
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction reversal = response.reverse()
                .execute("ICR");
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");

    }
    @Test
    public void test_cert_09_heartlandGiftCard_purchase() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.charge(new BigDecimal(11.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_cert_12_heartlandGiftCard_addValue_replenish() throws ApiException {
        Transaction response = heartlandGiftCardSwipe.addValue(new BigDecimal(12.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_13_heartlandGiftCard_return() throws ApiException {
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
        Transaction response = trans.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,response1.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber))
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_cert_14_heartlandGiftCard_authorizeCapture() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction response = heartlandGiftCardSwipe.authorize(new BigDecimal(25), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        String irr = response.getIssuerData().get(CardIssuerEntryTag.RetrievalReferenceNumber);
        NtsData ntsData = new NtsData();

        response.setNtsData(ntsData);
        Transaction captureResponse = response.capture(new BigDecimal(15.45))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,irr)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());

    }

    @Test
    public void test_cert_svs_activationReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                giftCard,
                "1200",
                "000001",
                "250213085404",
                "900060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_svs_returnReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                giftCard,
                "1200",
                "000001",
                "250213034159",
                "200060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_svs_preAuthReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                giftCard,
                "1200",
                "000001",
                "250213085404",
                "900060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_svs_activateReversal() throws ApiException {
        Transaction response = giftCard.activate(new BigDecimal(12))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction transaction1 = Transaction.fromNetwork(
                new BigDecimal(10),
                response.getAuthorizationCode(),
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                giftCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime(),
                response.getProcessingCode()
        );

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                giftCard,
                "1200",
                "000001",
                "250213085404",
                "900060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_HGC_activationReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                heartlandGiftCardSwipe,
                "1200",
                "000001",
                "250220084635",
                "900060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_HGC_purchaseReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                heartlandGiftCardSwipe,
                "1200",
                "000001",
                "250220082347",
                "006000"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_HGC_giftCard_return() throws ApiException {
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
    public void test_cert_HGC_returnReversal() throws ApiException {
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(FallbackCode.Received_IssuerTimeout,AuthorizerCode.Terminal_Authorized),
                heartlandGiftCardSwipe,
                "1200",
                "000001",
                "250220032307",
                "200060"
        );

        Transaction reversal = transaction.reverse()
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
        assertEquals(reversal.getMessageTypeIndicator(), "1420");
    }
    @Test
    public void test_cert_svs_auth_capture_single() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                "TYPE04",
                new NtsData(),
                giftCard,
                "1100",
                "000012",
                "250221073641"
        );

        Transaction captureResponse = transaction.capture(new BigDecimal(35.24))
                .withIssuerData(CardIssuerEntryTag.RetrievalReferenceNumber,"074307")
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());
    }
}
