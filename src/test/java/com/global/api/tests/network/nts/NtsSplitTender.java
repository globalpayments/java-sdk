package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
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
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsSplitTender {

    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";
    private CreditCardData card;
    private CreditTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private PriorMessageInformation priorMessageInformation;

    public NtsSplitTender() throws ConfigurationException {

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
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
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

        ntsRequestMessageHeader = new NtsRequestMessageHeader();

        ntsRequestMessageHeader.setTerminalDestinationTag("510");
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.NotPromptedPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        priorMessageInformation =new PriorMessageInformation();

        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

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

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);
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


    @Test
    public void test_partialApproved_Split_Tender_Amex_01() throws ApiException {

        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        track = new CreditTrackData();
        track.setValue(";371449635392376=25121019999888877776?");
        track.setEntryMethod(EntryMethod.Swipe);

        productData = getProductDataForNonFleetBankCards(track);

        Transaction response = track.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        System.out.println("STAN : " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertEquals(true, response.getTransactionReference().isPartialApproval());

        System.out.println("Transaction approved amount: " + response.getTransactionReference().getOriginalApprovedAmount());
    }

    @Test
    public void test_Split_Tender_MC_03() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
//        track = NtsTestCards.MasterCardTrack2(EntryMethod.MagneticStripeTrack2DataAttended);

        track = new CreditTrackData();
        track.setValue(";5506740000004316=12251019999888877776?");
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

        System.out.println("STAN : " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertEquals(true, response.getTransactionReference().isPartialApproval());
        System.out.println("Transaction approved amount: " + response.getTransactionReference().getOriginalApprovedAmount());
    }

    @Test
    public void test_Split_Tender_Visa_04() throws ApiException {
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        track = new CreditTrackData();
        track.setValue(";4427802641004797=12251011803939600000?");
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

        System.out.println("STAN : " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertEquals(true, response.getTransactionReference().isPartialApproval());
        System.out.println("Transaction approved amount: " + response.getTransactionReference().getOriginalApprovedAmount());
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

        System.out.println("STAN : " + response.getTransactionReference().getSystemTraceAuditNumber());
        assertEquals(true, response.getTransactionReference().isPartialApproval());
        System.out.println("Transaction approved amount: " + response.getTransactionReference().getOriginalApprovedAmount());

    }
}

