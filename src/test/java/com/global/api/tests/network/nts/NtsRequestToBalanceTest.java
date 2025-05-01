package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.FleetData;
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
        config.setUnitNumber("0001234567");
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
    private Transaction creditSaleWexEmv(double amount) throws ApiException {
        String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
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

        FleetData fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setServicePrompt("06");
        fleetData.setVehicleNumber("123456");
        fleetData.setUserId("123456");
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setDriversLicenseNumber("123456");
        fleetData.setEnteredData("123456");
        fleetData.setJobNumber("123456");
        fleetData.setDepartment("123456");
        fleetData.setOtherPromptCode("123456");

        track = NtsTestCards.WexFleetTrack2(EntryMethod.ContactEMV);
        Transaction response = track.charge(new BigDecimal(amount))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withTagData(emvTagData)
                .withCardSequenceNumber("101")
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

    @Test
    public void test_BatchCloseIssue_10358_MC16_70_79() throws ApiException {

        Transaction t = creditSale(10.11);
        Transaction t1 = creditSale(20.22);

        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RequestToBalacnce);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation = new PriorMessageInformation();
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

        Transaction retransmitBatchCloseResponse = NetworkService.resubmitBatchClose(batchClose.getTransactionToken())
                .execute();
        assertNotNull(retransmitBatchCloseResponse);

        NtsHostResponseCode responseCode = retransmitBatchCloseResponse.getNtsResponse().getNtsResponseMessageHeader().getNtsNetworkMessageHeader().getResponseCode();
        if (responseCode.equals(NtsHostResponseCode.FormatError)) {
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    Transaction transaction = NetworkService.resubmitBatchClose(retransmitBatchCloseResponse.getTransactionToken())
                            .execute();
                } else {
                    Transaction transaction = NetworkService.resubmitBatchClose(retransmitBatchCloseResponse.getTransactionToken())
                            .withHostResponseCode("79")
                            .execute();
                    BatchSummary newSummary = transaction.getBatchSummary();
                    assertNotNull(newSummary);
                }

            }
        }
    }
    @Test
    public void test_WexFleetEMV_RTB_01_Issue_10356() throws ApiException {
        Transaction t= creditSaleWexEmv(10.11);

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

        NtsRequestToBalanceData data = new NtsRequestToBalanceData(batchProvider.getSequenceNumber(), new BigDecimal(10.11), "Version");
        Transaction batchClose = BatchService.closeBatch(BatchCloseType.EndOfShift,
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 1
                        , new BigDecimal(10.11), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

        Transaction dataCollect = NetworkService.resubmitBatchClose(summary.getTransactionToken())
                .execute();
        assertNotNull(dataCollect);

    }

    private Transaction creditDataCollect(double amount) throws ApiException {
        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);


        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(amount))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

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

    private Transaction creditCreditAuthorization(double amount) throws ApiException {
        NtsRequestMessageHeader ntsRequestMessageHeader = new NtsRequestMessageHeader();
        ntsRequestMessageHeader.setTerminalDestinationTag("478");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);


        productData = getProductDataForNonFleetBankCards(track);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(amount))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

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

    @Test
    public void test_BatchCloseIssue_10359_retransmitDatacollect() throws ApiException {

        Transaction t= creditDataCollect(10.11);
        Transaction t1= creditCreditAuthorization(10.11);

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
                        ntsRequestMessageHeader, batchProvider.getBatchNumber(), 1
                        , new BigDecimal(30.33), BigDecimal.ONE, data)
                .execute();
        assertNotNull(batchClose);

        BatchSummary batchSummary = batchClose.getBatchSummary();
        LinkedList<String> list = new LinkedList<>();
        list.add(t.getTransactionToken());
        list.add(t1.getTransactionToken());

        BatchSummary summary = batchSummary.resubmitTransactions(list);
        assertNotNull(summary);

    }

}
