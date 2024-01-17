package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsRequestToBalanceData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class NtsRequestToBalanceTest {
    // gateway config
    NetworkGatewayConfig config;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;
    private BatchProvider batchProvider;
    private NtsTag16 tag;
    private NtsProductData productData;
    private CreditTrackData track;

    public NtsRequestToBalanceTest() throws ConfigurationException {
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);
        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("08");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        batchProvider = BatchProvider.getInstance();

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

        //  acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        // acceptorConfig.setPinCaptureCapability(PinCaptureCapability.Unknown);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(false);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);

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
    }
    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
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

    @Test //working
    public void test_RequestToBalance_06() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test //working
    public void test_RequestToBalance_16() throws ApiException {

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 1, 1
                        , new BigDecimal(10.11), new BigDecimal(11.11), data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test //working
    public void test_RequestToBalance_C6() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());
    }

    @Test
    public void test_RequestToBalance_D6() throws ApiException {

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");


        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

    }

    @Test
    public void test_BatchCloseIssue_10161() throws ApiException {

        creditSale(10.11);
        creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);

        //check batch summary
        assertNotNull(batchClose.getBatchSummary());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test
    public void test_BatchCloseIssue_withTotalDebitCreditAmount_10161() throws ApiException {

        creditSale(10.11);
        creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);

        //check batch summary
        assertNotNull(batchClose.getBatchSummary());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    @Test
    public void test_RequestToBalance_D6_resubmit_batch_close() throws ApiException {

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");


        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

        assertNotNull(batchClose.getTransactionToken());

        Transaction capture = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(capture);

    }
    @Test
    public void test_batch_resubmit_for_denial_response_code() throws ApiException {
            NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");
            Transaction transaction = creditSaleDataCollect(new BigDecimal(10));
            assertNotNull(transaction);
            assertNotNull(transaction.getTransactionToken());
            String creditSaleDataCollectToken = transaction.getTransactionToken();

            Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                            ntsRequestMessageHeader, 11, 0
                            , BigDecimal.ONE, BigDecimal.ONE, data)
                    .execute();
            assertNotNull(batchClose);

            BatchSummary batchSummary = batchClose.getBatchSummary();
            LinkedList<String> list = new LinkedList<>();
            list.add(creditSaleDataCollectToken);

            BatchSummary newSummary = batchSummary.resubmitTransactions(list);
            assertNotNull(newSummary);


    }

    // Credit Sale
    private Transaction creditSale(double amount) throws ApiException {
        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);


        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(amount))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        return response;
    }
    @Test //working
    public void test_RequestToBalance_HostResponse79() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ZERO, BigDecimal.ZERO, data)
                .withHostResponseCode("79")
                .execute();
        assertNotNull(batchClose);
        // check response
        assertEquals("00", batchClose.getResponseCode());

    }

    public Transaction creditSaleDataCollect(BigDecimal amount) throws ApiException {
        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);


        //track=TestCards.AmexSwipe(EntryMethod.Swipe);
        track=NtsTestCards.AmexTrack2(EntryMethod.Swipe);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.authorize(amount)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ForceCollectOrForceSale);

        Transaction dataCollectResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
        return dataCollectResponse;
    }
    @Test //working
    public void test_force_retransmit_RequestToBalance() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ONE, BigDecimal.ONE, data)
                .withHostResponseCode("79")
                .execute();
        assertNotNull(batchClose);

        Transaction t = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertEquals("00",t.getResponseCode());

    }
    @Test
    public void test_batch_resubmit_for_list_of_tokens() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");
        Transaction transaction = creditSaleDataCollect(new BigDecimal(10));
        assertNotNull(transaction);
        assertNotNull(transaction.getTransactionToken());
        String creditSaleDataCollectToken = transaction.getTransactionToken();

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ONE, BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);
        //06

        Transaction transactions = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(transactions);
        //16

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        assertNotNull(transactions.getTransactionToken());
        list.add(transactions.getTransactionToken());

        BatchSummary newSummary = batchSummary.resubmitTransactions(list,true);
        assertNotNull(newSummary);
        //D6

    }
    @Test
    public void test_BatchCloseIssue_10214() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        //check batch summary
        assertNotNull(batchClose.getBatchSummary());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

        // check response
        assertEquals("00", batchClose.getResponseCode());

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary newSummary = batchSummary.resubmitTransactions(list);
        assertNotNull(newSummary);
    }

    @Test
    public void test_batch_resubmit() throws ApiException {
        NtsRequestToBalanceData data = new NtsRequestToBalanceData(1, new BigDecimal(1), "Version");
        Transaction transaction = creditSaleDataCollect(new BigDecimal(10));
        assertNotNull(transaction);
        assertNotNull(transaction.getTransactionToken());
        String creditSaleDataCollectToken = transaction.getTransactionToken();

        Transaction batchClose = BatchService.closeBatch(BatchCloseType.Forced,
                        ntsRequestMessageHeader, 11, 0
                        , BigDecimal.ONE, BigDecimal.ONE, data)
                .execute();

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        assertNotNull(batchClose.getTransactionToken());
        list.add(batchClose.getTransactionToken());

        BatchSummary newSummary = batchSummary.resubmitTransactions(list);

        LinkedList<String> list1 = new LinkedList<>();
        assertNotNull(newSummary.getTransactionToken());
        list1.add(newSummary.getTransactionToken());

        BatchSummary newSummary1 = batchSummary.resubmitTransactions(list1,true);
        assertNotNull(newSummary1);

    }

    @Test
    public void test_BatchCloseIssue_withTotalDebitCreditAmount_10213() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        //check batch summary
        assertNotNull(batchClose.getBatchSummary());
        assertNotNull(batchClose.getBatchSummary().isNtsBalanced());

        // check response
        assertEquals("00", batchClose.getResponseCode());

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary newSummary = batchSummary.resubmitTransactions(list);
        assertNotNull(newSummary);
    }

    @Test
    public void test_BatchCloseIssue_10213_40_80_90() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

        BatchSummary newSummary1 = batchSummary.resubmitTransactions(false,summary.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary1);
        BatchSummary newSummary2 = batchSummary.resubmitTransactions(false,newSummary1.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary2);
        BatchSummary newSummary3 = batchSummary.resubmitTransactions(false,newSummary2.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary3);
        BatchSummary newSummary4 = batchSummary.resubmitTransactions(false,newSummary3.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary4);
        BatchSummary newSummary5 = batchSummary.resubmitTransactions(false,newSummary4.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary5);
        BatchSummary newSummary6 = batchSummary.resubmitTransactions(false,newSummary5.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary6);
        BatchSummary newSummary7 = batchSummary.resubmitTransactions(false,newSummary6.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary7);
        BatchSummary newSummary8 = batchSummary.resubmitTransactions(false,newSummary7.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary8);
        BatchSummary newSummary9 = batchSummary.resubmitTransactions(false,newSummary8.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary9);
        BatchSummary newSummary10 = batchSummary.resubmitTransactions(false,newSummary9.getNonApprovedDataCollectToken(),false);
        assertNotNull(newSummary10);

    }

    @Test
    public void test_BatchCloseIssue_10213_70_79() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

        ArrayList<String> tokens = new ArrayList<>(summary.getFormatErrorDataCollectToken());
        Transaction dataCollect = null;
        for(String token :tokens){
            for(int i = 0; i <= 1; i++){
                if(i ==0){
                    dataCollect = NetworkService.resubmitDataCollect(token)
                            .execute();
                }else if(!dataCollect.getResponseCode().equals("00")){
                    dataCollect = NetworkService.resubmitDataCollect(token)
                            .withHostResponseCode("79")
                            .execute();
                }
                assertNotNull(dataCollect);
            }
        }
    }
    @Test
    public void test_BatchCloseIssue_10214_16_to_C6() throws ApiException {

        creditSale(10.11);
        creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        // 16
        Transaction retransmitBatchCloseResponse = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(retransmitBatchCloseResponse);

        // C6
        Transaction forceBatchCloseResponse = NetworkService.resubmitBatchClose(retransmitBatchCloseResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(forceBatchCloseResponse);

    }

    @Test
    public void test_BatchCloseIssue_10213_RTB_01() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

        ArrayList<String> tokens = new ArrayList<>(summary.getNonApprovedDataCollectToken());
        for(String token :tokens){
            for(int i = 0; i < 10; i++){
                Transaction dataCollect = NetworkService.resubmitDataCollect(token)
                        .execute();
            }
        }

       ArrayList<String> list1 = new ArrayList<>();
        if(summary.getNonApprovedDataCollectToken() != null){
            list1.addAll(summary.getNonApprovedDataCollectToken());
        }
        if(summary.getAllDataCollectToken() != null){
            list1.addAll(summary.getAllDataCollectToken());
        }

        BatchSummary newSummary = batchSummary.resubmitTransactions(list1);
        assertNotNull(newSummary);

    }

    @Test
    public void test_BatchCloseIssue_10213_RTB_40() throws ApiException {

        Transaction t= creditSale(10.11);
        Transaction t1 =creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(1), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 2
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

        for(int i = 0; i < 10; i++){
            Transaction transaction = NetworkService.resubmitBatchClose(summary.getTransactionToken())
                    .execute();
            BatchSummary newSummary = transaction.getBatchSummary();
            assertNotNull(newSummary);

        }


    }
}
