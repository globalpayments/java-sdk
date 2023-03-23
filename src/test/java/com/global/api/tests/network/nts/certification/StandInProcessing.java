package com.global.api.tests.network.nts.certification;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.network.enums.nts.AvailableProductsCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import com.global.api.tests.testdata.TestCards;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StandInProcessing {
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private FleetData fleetData;
    private NtsProductData productData;
    private PriorMessageInfo priorMessageInfo;


    public StandInProcessing() throws ConfigurationException {
        acceptorConfig = new AcceptorConfig();

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

        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("510");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInfo=new PriorMessageInfo();
        priorMessageInfo.setPriorMessageResponseTime(1);
        priorMessageInfo.setPriorMessageConnectTime(999);
        priorMessageInfo.setPriorMessageCode("01");

        ntsRequestMessageHeader.setPriorMessageInfo(priorMessageInfo);

        fleetData = new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setDriverId("123456");

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

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
        config.setTerminalId("01");
        config.setUnitNumber("00001234567");
        config.setCompanyId("009");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);
        // MASTERCARD
        card = TestCards.MasterCardManual(true, true);
        track = new CreditTrackData();
        track.setValue(";5473500000000014=12251019999888877776?");
        track.setEntryMethod(EntryMethod.Swipe);


    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        NtsProductData productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons, 20.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.CarWash, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Dairy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Candy, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Milk, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.OilChange, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"), new BigDecimal(0));
        return productData;
    }

    @Ignore
    @Test //working
    public void test_visa_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = NtsTestCards.VisaTrack2(EntryMethod.Swipe);

        Transaction  response = track.charge(new BigDecimal(10))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(ntsRequestMessageHeader)
            .withUniqueDeviceId("0102")
            .withNtsProductData(getProductDataForNonFleetBankCards(track))
            .withCvn("123")
            .execute();

        assertNotNull(response);
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test //working
    public void test_visa_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.VisaTrack2(EntryMethod.Swipe);

        Transaction  response = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withCvn("123")
                    .execute();

            assertNotNull(response);
            assertEquals("80", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    @Ignore
    @Test //working
    public void test_visa_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.VisaTrack2(EntryMethod.Swipe);

         Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test
    public void test_MasterCard_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(90.90))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withCvn("123")
                    .execute();
        assertNotNull(response);
            // check response
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_MasterCard_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(90.90))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withCvn("123")
                    .execute();
            assertNotNull(response);
            // check response
            assertEquals("80", response.getResponseCode());


        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_MasterCard_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.MasterCardTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);
        // check response
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test //working
    public void test_Amex_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);


        Transaction chargeResponse = track.charge(new BigDecimal(10))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(ntsRequestMessageHeader)
            .withUniqueDeviceId("0102")
            .withNtsProductData(getProductDataForNonFleetBankCards(track))
            .withCvn("123")
            .execute();

        // check response
        assertEquals("40", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                chargeResponse.getOriginalTransactionDate(),
                chargeResponse.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test //working
    public void test_Amex_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .withUniqueDeviceId("0102")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withCvn("123")
                        .execute();

        // check response
        assertEquals("80", chargeResponse.getResponseCode());


        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                chargeResponse.getOriginalTransactionDate(),
                chargeResponse.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test //working
    public void test_Amex_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        track = NtsTestCards.AmexTrack1(EntryMethod.Swipe);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withCvn("123")
                    .execute();

        // check response
        assertEquals("90", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                chargeResponse.getOriginalTransactionDate(),
                chargeResponse.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test
    public void test_Discover_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(ntsRequestMessageHeader)
            .withUniqueDeviceId("0102")
            .withNtsProductData(getProductDataForNonFleetBankCards(track))
            .withCvn("123")
            .execute();
        assertNotNull(response);
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test
    public void test_Discover_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);
        Transaction  response = track.charge(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .withUniqueDeviceId("0102")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withCvn("123")
                        .execute();
        assertNotNull(response);
        assertEquals("80", response.getResponseCode());


        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test
    public void test_Discover_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = NtsTestCards.DiscoverTrack2(EntryMethod.Swipe);

        Transaction  response = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withCvn("123")
                    .execute();
        assertNotNull(response);
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_WexFleet_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);

        Transaction response = track.charge(new BigDecimal(10))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(ntsRequestMessageHeader)
            .withNtsProductData(getProductDataForNonFleetBankCards(track))
            .withFleetData(fleetData)
            .withCardSequenceNumber("101")
            .execute();

        assertNotNull(response);
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_WexFleet_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);
        Transaction  response = track.charge(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withFleetData(fleetData)
                        .withCardSequenceNumber("101")
                        .execute();

        assertNotNull(response);
        assertEquals("80", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_WexFleet_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";6900460430001234566=21121012202100000?");
        track.setEntryMethod(EntryMethod.Swipe);

        acceptorConfig.setAvailableProductCapability(AvailableProductsCapability.DeviceIsAvailableProductsCapable);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCardSequenceNumber("101")
                .execute();

        assertNotNull(response);
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test //working
    public void test_VisaFleet_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);


        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test //working
    public void test_VisaFleet_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withFleetData(fleetData)
                .withCvn("123")
                .execute();

        assertNotNull(response);
        assertEquals("80", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test //working
    public void test_VisaFleet_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";4484630000000126=25121011000062111401?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction  response = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(getProductDataForNonFleetBankCards(track))
                    .withFleetData(fleetData)
                    .withCvn("123")
                    .execute();

        assertNotNull(response);
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_VoyagerFleet_DataCollect_Stand_In_processing_CTF_40() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
            .withCurrency("USD")
            .withNtsRequestMessageHeader(ntsRequestMessageHeader)
            .withUniqueDeviceId("0102")
            .withNtsProductData(productData)
            .withFleetData(fleetData)
            .execute();

        assertNotNull(response);
        assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_VoyagerFleet_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.charge(new BigDecimal(90.90))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsProductData(productData)
                    .withFleetData(fleetData)
                    .execute();

        assertNotNull(response);
        assertEquals("80", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Ignore
    @Test
    public void test_VoyagerFleet_DataCollect_Stand_In_processing_CTF_90() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Plus, UnitOfMeasure.Gallons, 10.24, 1.259);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus2, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.RegularM5, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.addNonFuel(NtsProductCode.Plus3, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

         Transaction response = track.charge(new BigDecimal(90.90))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsProductData(productData)
                .withFleetData(fleetData)
                .execute();

         assertNotNull(response);
         assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Ignore
    @Test //working
    public void test_MasterCardFleet_DataCollect_Stand_In_processing_CTF_40() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?"); // sample test track 2 data.
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

         Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

         assertNotNull(response);
         assertEquals("40", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }
    @Ignore
    @Test //working
    public void test_MasterCardFleet_DataCollect_Stand_In_processing_CTF_80() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?"); // sample test track 2 data.
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction  response = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withFleetData(fleetData)
                    .withNtsProductData(productData)
                    .withCvn("123")
                    .execute();

        assertNotNull(response);
        assertEquals("80", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }
    @Ignore
    @Test //working
    public void test_MasterCardFleet_DataCollect_Stand_In_processing_CTF_90() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";5567300000000016=25121019999888877711?"); // sample test track 2 data.
        track.setEntryMethod(EntryMethod.Swipe);

        productData = new NtsProductData(ServiceLevel.FullServe, track);
        productData.addFuel(NtsProductCode.Lng, UnitOfMeasure.Gallons, 1, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries, UnitOfMeasure.NoFuelPurchased, 1, 10.74);
        productData.add(new BigDecimal(32.33), new BigDecimal(0));
        productData.setProductCodeType(ProductCodeType.IdnumberAndOdometerOrVehicleId);

        Transaction  response = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withFleetData(fleetData)
                    .withNtsProductData(productData)
                    .withCvn("123")
                    .execute();

        assertNotNull(response);
        assertEquals("90", response.getResponseCode());

        // Data-Collect request preparation.
        // Data-Collect request preparation.
        Transaction transaction = Transaction.fromNetwork(
                AuthorizerCode.Terminal_Authorized,
                "123456",
                "08",
                response.getOriginalTransactionDate(),
                response.getOriginalTransactionTime(),
                track
        );

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(productData)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());

    }
}