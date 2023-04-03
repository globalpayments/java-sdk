package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
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

public class NtsPartialApprovalTests {
    NtsTag16 tag;
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;



    public NtsPartialApprovalTests() throws ApiException {

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

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
        NetworkGatewayConfig config = new NetworkGatewayConfig(Target.NTS);
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

        //ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

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

    @Test //working
    public void test_Auth_Partial_Approval_MasterCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B5473500000000014^MASTERCARD TEST^25121041234567890123?9"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.authorize(new BigDecimal(130))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_Auth_Partial_Approval_VisaCard_Credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B4484104292153662^POSINT TEST VISA P CARD^2512501032100321001000?");// track2 data
        track.setEntryMethod(EntryMethod.Swipe);
        Transaction response = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_Auth_Partial_Approval_DiscoverCard_Credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

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
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_Auth_Partial_Approval_AmexCard_Credit() throws ApiException {

        track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Amex");

        Transaction response = track.authorize(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test //working 01
    public void test_Sale_Partial_Approval_MasterCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue("%B5473500000000014^MASTERCARD TEST^25121041234567890123?9"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(130))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test // working
    public void test_Sale_Partial_Approval_VisaCard_Credit() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue("%B4484104292153662^POSINT TEST VISA P CARD^2512501032100321001000?");// track2 data
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test // working
    public void test_Sale_Partial_Approval_DiscoverCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue("%B6011000990156527^DIS TEST CARD^25121011000062111401?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Discover");

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test // working
    public void test_Sale_Partial_Approval_AmexCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Amex");

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Partial_Approval_Balance_Inquiry_MasterCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B5473500000000014^MASTERCARD TEST^25121041234567890123?9"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response = track.balanceInquiry()
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withAmount(new BigDecimal(0))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Partial_Approval_Balance_Inquiry_VisaCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B4484104292153662^POSINT TEST VISA P CARD^2512501032100321001000?");// track2 data
        track.setEntryMethod(EntryMethod.Swipe);
        ;

        Transaction response = track.balanceInquiry()
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withAmount(new BigDecimal(0))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Partial_Approval_Balance_Inquiry_AmexCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B372700699251018^AMEX TEST CARD^2512990502700?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Amex");

        Transaction response = track.balanceInquiry()
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withAmount(new BigDecimal(0))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Partial_Approval_Balance_Inquiry_DiscoverCard_Credit() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue("%B6011000990156527^DIS TEST CARD^25121011000062111401?"); // sample test track 1 data.
        track.setEntryMethod(EntryMethod.Swipe);
        track.setCardType("Discover");

        Transaction response = track.balanceInquiry()
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withAmount(new BigDecimal(0))
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

}
