package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Nts3DESGiftEncryptionTests {
    private NtsRequestMessageHeader header; //Main Request header class
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private GiftCard giftCard;
    private PriorMessageInformation priorMessageInformation;
    private NtsTag16 tag;
    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));

    public Nts3DESGiftEncryptionTests() throws ConfigurationException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        // Address details.
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        // Setting operating environment
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        acceptorConfig.setSupportedEncryptionType(EncryptionType.TDES);
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setOperationType(OperationType.Decrypt);

        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("510");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation = new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");

        header.setPriorMessageInformation(priorMessageInformation);

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        // gateway config
        config = new NetworkGatewayConfig(Target.NTS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setAcceptorConfig(acceptorConfig);

        // NTS Related configurations
        config.setBinTerminalId(" ");
        config.setBinTerminalType(" ");
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);
        config.setTerminalId("21");
        config.setUnitNumber("00001234567");
        config.setSoftwareVersion("01");
        config.setCompanyId("045");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        //prepaid card SVS account number
        giftCard = new GiftCard();
        giftCard.setEncryptionData(EncryptionData.setKtbAndKsn("D87A55F042D1DA9DAD3959DAAE8C3A423E27412D58669AA86993049F07662E478E75B439D9279790",
                "F000019990E00003"));
        giftCard.setEncryptedPan("49FE802FA87C5984BBE68AE7A3277200");
        giftCard.setTrackNumber(TrackNumber.TrackTwo);
        giftCard.setEntryMethod(EntryMethod.Swipe);
//        giftCard.setCardType("StoredValue");
        giftCard.setCardType("HeartlandGift");
        giftCard.setExpiry("2501");
    }

    @Test
    public void test_SVS_active_001() throws ApiException {

        Transaction response = giftCard.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_SVS_Pre_Authorization_008() throws ApiException {
        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_SVS_Balance_Inquiry_004() throws ApiException {
        Transaction response = giftCard.balanceInquiry()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void svs_replenish() throws ApiException {

        Transaction response = giftCard.addValue(new BigDecimal(10.00))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test//working
    public void test_SVS_pre_authorization_Completion_07() throws ApiException {

        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction dataCollectResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }

    @Test//working
    public void test_HGC_preAuth_reversal_13() throws ApiException {

        Transaction response = giftCard.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        TransactionReference ts = response.getTransactionReference().setOriginalTransactionTypeIndicator(TransactionTypeIndicator.PreAuthorization);
        response.setTransactionReference(ts);
        Transaction dataCollectResponse = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_HGC_sale_008() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_HGC_cancel_activate_001() throws ApiException {

        Transaction transaction = giftCard.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        assertNotNull(transaction);

        // check response
        assertEquals("00", transaction.getResponseCode());
        TransactionReference ts = transaction.getTransactionReference().setOriginalTransactionTypeIndicator(TransactionTypeIndicator.CardActivation);
        transaction.setTransactionReference(ts);

        Transaction response = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_HGC_sale_Cancel() throws ApiException {
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    TransactionReference reference = response.getTransactionReference().setOriginalTransactionTypeIndicator(TransactionTypeIndicator.Purchase);
        response.setTransactionReference(reference);

    Transaction cancel = response.voidTransaction(new BigDecimal(10))
            .withNtsRequestMessageHeader(header)
            .withSystemTraceAuditNumber(Stan)
            .execute();

    // check response
    assertEquals("00", cancel.getResponseCode());

    }
    @Test
    public void test_HGC_refund_Cancel() throws ApiException {
        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        TransactionReference reference = response.getTransactionReference().setOriginalTransactionTypeIndicator(TransactionTypeIndicator.MerchandiseReturn);
        response.setTransactionReference(reference);

        Transaction cancel = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();
        // check response
        assertEquals("00", cancel.getResponseCode());

    }

    @Test
    public void giftCard_cash_out() throws ApiException {
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        Transaction response = giftCard.cashOut()
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }
}
