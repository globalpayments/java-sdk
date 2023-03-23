package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.nts.NtsAuthCreditResponseMapper;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsGiftTests {
    // gateway config
    NetworkGatewayConfig config;
    AcceptorConfig acceptorConfig;
    private GiftCard card;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));
    private PriorMessageInfo priorMessageInfo;

    public NtsGiftTests() throws ConfigurationException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);
        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInfo=new PriorMessageInfo();
        priorMessageInfo.setPriorMessageResponseTime(999);
        priorMessageInfo.setPriorMessageConnectTime(999);
        priorMessageInfo.setPriorMessageCode("08");

        ntsRequestMessageHeader.setPriorMessageInfo(priorMessageInfo);


        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        //  acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        // acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);

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
        // gateway config
        config = new NetworkGatewayConfig(Target.NTS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
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
        config.setUnitNumber("00066654534");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

//        card = NtsTestCards.svsCard();
        card = NtsTestCards.svsCardTrack1(EntryMethod.Swipe);
    }

    private NtsTag16 getTag16(){
        NtsTag16 tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Full);
        tag.setSecurityData(SecurityData.CVN);
        return tag;
    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,10.24, 1.259);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons,20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    @Test
    public void test_SVS_active_001() throws ApiException {
        Transaction response = card.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_active_cancellation_002() throws ApiException {
        Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));

        Transaction response = card.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        Transaction transaction = Transaction.fromBuilder()
                .withTransactionTypeIndicator(response.getTransactionReference().getOriginalTransactionTypeIndicator())
                .withAuthorizer(response.getTransactionReference().getAuthorizer())
                .withPaymentMethod(card)
                .withOriginalMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry.getValue())
                .withApprovalCode(response.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withSystemTraceAuditNumber(response.getTransactionReference().getSystemTraceAuditNumber())
                .withTransactionTime(response.getTransactionReference().getOriginalTransactionTime())
                .withOriginalTransactionDate(response.getTransactionReference().getOriginalTransactionDate())
                .build();

        response = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(transaction.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_active_reversal_003() throws ApiException {

        Transaction response = card.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt( response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();


        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Balance_Inquiry_004() throws ApiException {

//

        Transaction response = card.balanceInquiry()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Issue_005() throws ApiException {

        Transaction response = card.issue(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Issue_Cancellation_006() throws ApiException {

        Transaction response = card.issue(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Issue_Reversal_007() throws ApiException {



        Transaction response = card.issue(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt( response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Pre_Authorization_008() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Pre_Authorization_Completion_009() throws ApiException {
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        Transaction completionResponse = response.preAuthCompletion(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber( Integer.parseInt( response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", completionResponse.getResponseCode());

        assertNotNull(completionResponse.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + completionResponse.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(completionResponse.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + completionResponse.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(completionResponse.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + completionResponse.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(getTag16())
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_SVS_Pre_Authorization_Reversal_010() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Pre_Authorization_ICR_011() throws ApiException {
        ServicesContainer.configureService(config, "ICR");
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute("ICR");

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

    }

    @Test
    public void test_SVS_Pre_Authorization_Completion_ICR_012() throws ApiException {
        ServicesContainer.configureService(config, "ICR");
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute("ICR");

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        Transaction completionResponse = response.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute("ICR");

        // check response
        assertEquals("00", completionResponse.getResponseCode());

        assertNotNull(completionResponse.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + completionResponse.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(completionResponse.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + completionResponse.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(completionResponse.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + completionResponse.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute("ICR");
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_SVS_Pre_Authorization_Reversal_ICR_013() throws ApiException {
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Purchase_014() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction transaction = Transaction.fromNetwork(response.getTransactionReference().getAuthorizer(),
                response.getTransactionReference().getApprovalCode(),
                response.getResponseCode(),
                response.getTransactionReference().getOriginalTransactionTypeIndicator(),
                response.getTransactionReference().getSystemTraceAuditNumber(),
                response.getTransactionReference().getOriginalTransactionDate(),
                response.getTransactionReference().getOriginalTransactionTime(),
                card);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(transaction.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_SVS_Purchase_Cancellation_015() throws ApiException {

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Purchase_Reversal_016() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Recharge_017() throws ApiException {
        Transaction response = card.addValue(new BigDecimal(50))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Recharge_Reversal_018() throws ApiException {
        Transaction response = card.addValue(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Return_019() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Return_Reversal_020() throws ApiException {
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

    @Test
    public void test_SVS_Purchase_Split_Tender_021() throws ApiException {
        Transaction response = card.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("15", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        NtsAuthCreditResponseMapper responseMapper = (NtsAuthCreditResponseMapper) response.getNtsResponse().getNtsResponseMessage();

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_SVS_Purchase_Reversal_Split_Tender_022() throws ApiException {
        Transaction response = card.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("15", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = response.reverse(new BigDecimal(100))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }


    @Test
    public void test_001_cert() throws ApiException {
        Transaction response = card.activate(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));

        response = card.addValue(new BigDecimal(50))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        assertNotNull(response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        System.out.println("TransactionTypeIndicator: " + response.getTransactionReference().getOriginalTransactionTypeIndicator().getValue());
        assertNotNull(response.getTransactionReference().getSystemTraceAuditNumber());
        System.out.println("STAN: " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertNotNull(response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
        System.out.println("RemainingBalance: " + response.getTransactionReference().getBankcardData().get(UserDataTag.RemainingBalance));
    }

}
