package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
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
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtsIcrTest {
    private CreditCardData card;
    private CreditTrackData track;;
    private com.global.api.paymentMethods.DebitTrackData DebitTrackData;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    NtsTag16 tag;
    private FleetData fleetData;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private PriorMessageInformation priorMessageInformation;
    public NtsIcrTest() throws ApiException {

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended); //ICR


        // Address details.
        Address address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("510");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");

        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        acceptorConfig.setAddress(address);

        fleetData=new FleetData();
        fleetData.setPurchaseDeviceSequenceNumber("12345");
        fleetData.setDriverId("123456");

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setCapableAmexRemainingBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setCapableVoid(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setMobileDevice(true);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");


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

        // with merchant type
        config.setMerchantType("5541");
        ServicesContainer.configureService(config, "ICR");

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);
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


    @Test //working
    public void test_ICR_Auth_Partial_Approval_MasterCard_Credit() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%B5473500000000014^MASTERCARD TEST^25121041234567890123?9"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute("ICR");
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        Transaction responseSale = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute("ICR");
        assertNotNull(responseSale);

        // check response
        assertEquals("00", responseSale.getResponseCode());
    }
    @Test // working
    public void test_ICR_Auth_Partial_Approval_VisaCard_Credit() throws ApiException {
        {
            track = new CreditTrackData();
            track.setValue("%B4484104292153662^POSINT TEST VISA P CARD^2512501032100321001000?");// track2 data
            track.setEntryMethod(EntryMethod.Swipe);

            Transaction response = track.authorize(new BigDecimal(142))
                    .withCurrency("USD")
                    .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                    .withUniqueDeviceId("0102")
                    .withNtsTag16(tag)
                    .withCvn("123")
                    .execute("ICR");
            assertNotNull(response);

            // check response
            assertEquals("00", response.getResponseCode());
        }
    }
    @Test //working
    public void test_ICR_Auth_Partial_Approval_DiscoverCard_Credit() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%B6011000990156527^DIS TEST CARD^25121011000062111401?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Discover");

        Transaction response = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute("ICR");
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test //working
    public void test_ICR_Auth_Approval_AmexCard_Credit() throws ApiException {
        track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
//        track.setCardType("Amex");

        Transaction authResponse = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute("ICR");
        assertNotNull(authResponse);

        // check respons
        assertEquals("00", authResponse.getResponseCode());
        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = authResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(tag)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute("ICR");
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }


    @Test
    public void test_001_Voyager_Fleet_ICR_pre_authorization() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
    @Test //working
    public void test_002_Voyager_Fleet_ICR_pre_authorization() throws ApiException {
        track = new CreditTrackData();
        track.setValue(";7088869008250005031=25120000000000000?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setExpiry("1225");

        fleetData = new FleetData();
        fleetData.setOdometerReading("4800012");
        fleetData.setVehicleNumber("123456");

        Transaction authResponse = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(authResponse);
        assertEquals("00", authResponse.getResponseCode());
    }
}

