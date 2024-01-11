package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class NtsDebitTest {
    private DebitTrackData track;
    private NtsRequestMessageHeader ntsRequestMessageHeader;
    private PriorMessageInformation priorMessageInformation;
    AcceptorConfig acceptorConfig;
    // gateway config
    NetworkGatewayConfig config;
    String emvTagData = "4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401019F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000";

    public NtsDebitTest() throws ApiException {
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.PinDebit);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("999");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("08");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

        // data code values
        // acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.None);
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactlessMsd_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);

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
        config.setUnitNumber("00066654534");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        ServicesContainer.configureService(config);

//        config.setMerchantType("5541");
        ServicesContainer.configureService(config, "ICR");


        ServicesContainer.configureService(config, "timeout");
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setKsn("A504010005E0003C    ");

        // debit card
        track = new DebitTrackData();
        track.setValue(";720002123456789=2512120000000000001?9  ");
         track.setEntryMethod(EntryMethod.Swipe); //For EMV test cases
        track.setPinBlock("78FBB9DAEEB14E5A504010005E0003C     ");
        track.setCardType("PinDebit");
    }

    private NtsTag16 getTag16() {
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

    @Test //working
    public void test_PinDebit_with_Purchase_03() throws ApiException {
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv_MagStripe);

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }


    @Test //working
    public void test_PinDebit_with_Purchase_03_EMV() throws ApiException {

        Transaction response = track.charge(new BigDecimal(142))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_pre_authorization_06() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    @Test //Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthorization_06() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    @Test // Working
    public void test_PinDebit_Offline_Verified_EMV_With_Track2Format_PreAuthorization_06() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
//        config.setInputCapabilityCode(CardDataInputCapability.ContactlessEmv);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
//        track.setEntryMethod(EntryMethod.ContactlessEMV);
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

    }

    @Test // Working
    public void test_PinDebit_Offline_Verified_EMV_With_Track2Format_PreAuthorization_Completion_() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
//        config.setInputCapabilityCode(CardDataInputCapability.ContactlessEmv);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
//        track.setEntryMethod(EntryMethod.ContactlessEMV);
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());


    }

    @Test // Working
    public void test_PinDebit_Offline_Verified_EMV_Track2Format_purchase_06() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        ServicesContainer.configureService(config);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .withTagData(emvTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());
    }

    @Test // working
    public void test_PinDebit_pre_authorization_cancellation_without_TrackData_08() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        Transaction preAuthorizationFundsResponse = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withModifier(TransactionModifier.Offline) // Only for the offline approved transactions.
                .withOfflineAuthCode("")
                .execute();
        assertNotNull(preAuthorizationFundsResponse);
        assertEquals("00", preAuthorizationFundsResponse.getResponseCode());


        Transaction voidResponse = preAuthorizationFundsResponse.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    //    @Test //not working
//    public void test_PinDebit_with_Purchase_03_MC_PartialApproval() throws ApiException {
//        ntsRequestMessageHeader.setntsRequestMessageHeader(ntsRequestMessageHeader);
//
//        config.setMessageCode(NtsMessageCode.DataCollectOrSale);
//        track.setEntryMethod(EntryMethod.Manual_attended); //For Master card
//        track.setCardType("MC");
//
//        Transaction response = track.charge(new BigDecimal(33))
//                .withCurrency("USD")
//                .withTransactionCode(TransactionCode.Purchase)
//               // .withntsRequestMessageHeader(ntsRequestMessageHeader)
//                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
//                .withUniqueDeviceId("0001")
//                .execute();
//        assertNotNull(response);
//
//        // check response
//        assertEquals("00", response.getResponseCode());
//    }
    @Test // working
    public void test_PinDebit_Purchase_03_With_DataCollect_02() throws ApiException {
        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(chargeResponse);
        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction transaction = Transaction.fromNetwork(
                chargeResponse.getTransactionReference().getAuthorizer(),
                chargeResponse.getTransactionReference().getApprovalCode(),
                chargeResponse.getTransactionReference().getDebitAuthorizer(),
                chargeResponse.getOriginalTransactionDate(),
                chargeResponse.getOriginalTransactionTime(),
                track
        );

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_PinDebit_Purchase_03_With_DataCollect_02_With_UserData() throws ApiException {
        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(chargeResponse);

        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_PinDebit_Purchase_03_With_DataCollect_12() throws ApiException {
        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(chargeResponse);

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitDataCollect);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_PinDebit_Purchase_03_With_DataCollect_C2() throws ApiException {
        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(chargeResponse);

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ForceCollectOrForceSale);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_PinDebit_Purchase_03_With_DataCollect_D2() throws ApiException {
        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(chargeResponse);

        // check response
        assertEquals("00", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitForceCollect);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_purchase_with_cashBack_04() throws ApiException {

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_purchase_with_cashBack_04_With_DataCollect_02() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_purchase_with_cashBack_04_With_DataCollect_12() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitDataCollect);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_purchase_with_cashBack_04_With_DataCollect_C2() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ForceCollectOrForceSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_purchase_with_cashBack_04_With_DataCollect_D2() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        String transactionDate = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionDate();
        String transactionTime = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionTime();

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitForceCollect);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test//
    public void test_PinDebit_purchase_refund_05() throws ApiException {

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());


        Transaction refund = track.refund(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());

    }

    @Test// working
    public void test_PinDebit_purchase_refund_05_with_Credit_Adjustment_03() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        // Data-Collect request preparation.
        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)

                .withCurrency("USD")
                .execute();

        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());
    }

    @Test// working
    public void test_PinDebit_purchase_refund_05_with_Credit_Adjustment_03_With_UserData() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())

                .withCurrency("USD")
                .execute();

        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());
    }

    @Test// working
    public void test_PinDebit_purchase_refund_05_with_Credit_Adjustment_13() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitCreditAdjustment);

        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())

                .withCurrency("USD")
                .execute();

        assertNotNull(refund);

        assertEquals("00", refund.getResponseCode());
    }

    @Test// working
    public void test_PinDebit_purchase_refund_05_with_Credit_Adjustment_C3() throws ApiException {
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        String transactionDate = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionDate();
        String transactionTime = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionTime();

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.ForceCreditAdjustment);

        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withCurrency("USD")
                .execute();

        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());
    }

    @Test// working
    public void test_PinDebit_purchase_refund_05_with_Credit_Adjustment_D3() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());


        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.RetransmitForceCreditAdjustment);

        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withCurrency("USD")
                .execute();

        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());
    }

    @Test//working
    public void test_PinDebit_pre_authorization_ICR_06() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute("ICR");
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }

    @Test //working
    public void test_PinDebit_pre_authorization_completion_without_TrackData_07() throws ApiException {
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());

        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        // check response
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test // working
    public void test_PinDebit_pre_authorization_completion_ICR_without_TrackData_07() throws ApiException {

        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute("ICR");
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());


        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        // check response
        assertEquals("00", captureResponse.getResponseCode());
    }

    @Test // working
    public void test_PinDebit_pre_authorization_Cancellation_ICR_without_TrackData_08() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute("ICR");
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(response.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withOriginalMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry.getValue())
                .withApprovalCode(response.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(response.getAuthorizationCode())
                .withSystemTraceAuditNumber(response.getTransactionReference().getSystemTraceAuditNumber())
                .withTransactionTime(response.getTransactionReference().getOriginalTransactionTime())
                .withOriginalTransactionDate( response.getTransactionReference().getOriginalTransactionDate())
                .build();

        Transaction voidResponse = transaction.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        // check response
        assertEquals("00", voidResponse.getResponseCode());
    }

    @Test //Need to fix this TC
    @Ignore
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    @Test//working
    public void test_pinDebit_purchase_reversal_13() throws ApiException {
        Transaction reversalResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(reversalResponse);
        assertEquals("00", reversalResponse.getResponseCode());


        Transaction refund = reversalResponse.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);

        assertEquals("00", refund.getResponseCode());

    }

    @Test// working
    public void test_purchase_with_cashBack_reversal_14() throws ApiException {
        Transaction reversalResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withCashBack(new BigDecimal(3))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(reversalResponse);

        assertEquals("00", reversalResponse.getResponseCode());


        Transaction reversal = reversalResponse.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(3))
                .withCurrency("USD")
                .execute();

        assertEquals("00", reversal.getResponseCode());
    }

    @Test// working
    //A Purchase Return Reversal message is used when a time-out or a HOST RESPONSE CODE
    //80 is received on a Purchase Return (05).
    public void test_purchase_refund_reversal_15() throws ApiException {

        Transaction refundResponse = track.refund(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refundResponse);

        assertEquals("00", refundResponse.getResponseCode());
        Transaction reversalResponse = refundResponse.reverse(new BigDecimal(10))

                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(reversalResponse);

        assertEquals("00", reversalResponse.getResponseCode());
    }


    @Test//working
    public void test_pinDebit_purchase_reversal_13_With_EMV() throws ApiException {
        Transaction reversalResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();
        assertNotNull(reversalResponse);
        assertEquals("00", reversalResponse.getResponseCode());

        Transaction refund = reversalResponse.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCurrency("USD")
                .execute();
        assertNotNull(refund);

        assertEquals("00", refund.getResponseCode());
    }

    @Test
    public void test_force_resubmit_using_token() throws ApiException {
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute("ICR");
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());


        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        Transaction dataCollectResponse = captureResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", captureResponse.getResponseCode());
        Transaction resubmitResponse = NetworkService.resubmitDataCollect(dataCollectResponse.getTransactionToken())
                .withForceToHost(true)
                .execute();

        assertNotNull(resubmitResponse);
    }

    @Test// working
    public void test_PinDebit_purchase_refund_force_retransmit() throws ApiException {

        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction refund = response.capture(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())

                .withCurrency("USD")
                .execute();

        assertNotNull(refund);


        assertEquals("00", refund.getResponseCode());

        Transaction transaction = NetworkService.resubmitDataCollect(refund.getTransactionToken())
                .withForceToHost(true)
                .execute();
        assertNotNull(transaction);
    }

    //with card sequence number
    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10189() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("123")
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10189_Negative() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withCardSequenceNumber("1234")
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    // WithOut Card Sequence Number
    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10189_withoutCSN() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working With Offline Decline Indicator
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10190_withODI() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withOfflineDeclineIndicator("Y")
                .execute("ICR");

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10191() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withUniqueDeviceId("1234")
                .execute("ICR");

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10191_Negative() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .withUniqueDeviceId("12345")
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10189_90_91() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withCardSequenceNumber("123")
                .withUniqueDeviceId("1234")
                .withTagData(emvTagData)
                .withOfflineDeclineIndicator("Y")
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }

    @Test // Working
    public void test_PinDebit_EMV_With_Track2Format_PreAuthCancel_10189_90_91_Negative_WithOutData() throws ApiException {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.PinValidate);
        config.setInputCapabilityCode(CardDataInputCapability.ContactEmv);
        ServicesContainer.configureService(config, "ICR");
        track.setEntryMethod(EntryMethod.ContactEMV);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withUniqueDeviceId("0001")
                .withCardSequenceNumber("1234")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction preAuthCompletion = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTagData(emvTagData)
                .execute();

        assertEquals("00", preAuthCompletion.getResponseCode());
    }
    @Test //working as expected -- need to look into userdata length
    public void test_preauthCompletion_Datacollect_Issue_10195_withOutTimeStamp() throws ApiException {
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());

        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        assertEquals("00", captureResponse.getResponseCode());

   //      Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(preAuthorizationResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(preAuthorizationResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(preAuthorizationResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(preAuthorizationResponse.getAuthorizationCode())
                .withOriginalTransactionDate(preAuthorizationResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(preAuthorizationResponse.getTransactionReference().getOriginalTransactionTime())
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);
        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_preauthCompletion_Datacollect_Issue_10195_withTimeStamp() throws ApiException {
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTimestamp("230524035010")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());

        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTimestamp("230524035020")
                .withNtsTag16(getTag16())
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        // check response
        assertEquals("00", captureResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(preAuthorizationResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(preAuthorizationResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(preAuthorizationResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(preAuthorizationResponse.getAuthorizationCode())
                .withOriginalTransactionDate(preAuthorizationResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(preAuthorizationResponse.getTransactionReference().getOriginalTransactionTime())
                .build();

        Transaction dataCollectResponse = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withTimestamp("230524035030")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test //working
    public void test_preauthCompletion_Datacollect_Issue_10195_DataCollect_retry() throws ApiException {
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTimestamp("230523090510")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());

        Transaction captureResponse = preAuthorizationResponse.preAuthCompletion(new BigDecimal(10))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withTimestamp("230523090520")
                .withNtsTag16(getTag16())
                .withSettlementAmount(new BigDecimal(10))
                .execute();

        // check response
        assertEquals("00", captureResponse.getResponseCode());

        // Data-Collect request preparation.
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction transaction = Transaction.fromBuilder()
                .withAuthorizer(preAuthorizationResponse.getTransactionReference().getAuthorizer())
                .withPaymentMethod(track)
                .withDebitAuthorizer(preAuthorizationResponse.getTransactionReference().getDebitAuthorizer())
                .withApprovalCode(preAuthorizationResponse.getTransactionReference().getApprovalCode())
                .withAuthorizationCode(preAuthorizationResponse.getAuthorizationCode())
                .withOriginalTransactionDate(preAuthorizationResponse.getTransactionReference().getOriginalTransactionDate())
                .withTransactionTime(preAuthorizationResponse.getTransactionReference().getOriginalTransactionTime())
                .build();

        Transaction dataCollectResponse1 = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withTimestamp("230523090530")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse1);
        // check response
        assertEquals("00", dataCollectResponse1.getResponseCode());

        //Data collect retries
        Transaction dataCollectResponse2 = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withTimestamp("230523090540")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse2);
        // check response
        assertEquals("00", dataCollectResponse2.getResponseCode());

        //Data collect retries
        Transaction dataCollectResponse3 = transaction.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withTimestamp("230523090550")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse3);
        // check response
        assertEquals("00", dataCollectResponse3.getResponseCode());
    }

    @Test
    public void test_Preauth_DataCollect_DateAndTime() throws ApiException{
        Transaction preAuthorizationResponse = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withTimestamp("230523090510")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(preAuthorizationResponse);

        assertEquals("00", preAuthorizationResponse.getResponseCode());

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = preAuthorizationResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withTimestamp("230523090540")
                .withNtsTag16(getTag16())
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);
    }

    // used only for code coverage
    @Test
    public void test_DataCollect_IncorrectFormat_CodeCoverageOnly() throws ApiException {

        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0713")
                .withTransactionTime("090530")
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .build();

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        GatewayException incorrectFormatException = assertThrows(GatewayException.class,
                () -> transaction.capture(new BigDecimal(10))
                        .withCurrency("USD")
                        .withTimestamp("230523090550")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .withSettlementAmount(new BigDecimal(5))
                        .withNtsTag16(getTag16())
                        .execute());
    }

    @Test
    public void test_PinDebit_withdrawalReversal_CodeCoverage() {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0821")
                .withTransactionTime("090520")
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .build();

        TransactionReference reference = new TransactionReference();
        reference.setOriginalPaymentMethod(track).setOriginalTransactionCode(TransactionCode.Withdrawal);
        reference.setOriginalPaymentMethod(track).setOriginalAmount(new BigDecimal(10));
        reference.setOriginalTransactionTime("095550");
        reference.setOriginalTransactionDate("0729");
        transaction.setTransactionReference(reference);

        GatewayException incorrectFormat2 = assertThrows(GatewayException.class,
                () -> transaction.reverse(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withNtsTag16(getTag16())
                        .withSettlementAmount(new BigDecimal(12))
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .execute());
        assertNotNull(incorrectFormat2);
    }

    @Test
    public void test_PinDebit_purchaseReturnReversal_CodeCoverage() {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0821")
                .withTransactionTime("090520")
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .build();

        TransactionReference reference = new TransactionReference();
        reference.setOriginalPaymentMethod(track).setOriginalTransactionCode(TransactionCode.PurchaseReturn);
        reference.setOriginalPaymentMethod(track).setOriginalAmount(new BigDecimal(10));
        reference.setOriginalTransactionTime("095550");
        reference.setOriginalTransactionDate("0729");
        transaction.setTransactionReference(reference);

        GatewayException incorrectFormat2 = assertThrows(GatewayException.class,
                () -> transaction.reverse(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withNtsTag16(getTag16())
                        .withSettlementAmount(new BigDecimal(12))
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .execute());
        assertNotNull(incorrectFormat2);
    }

    @Test
    public void test_PreauthCompletion_purchaseReturn_CodeCoverage() {
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        Transaction transaction = Transaction.fromBuilder()
                .withPaymentMethod(track)
                .withDebitAuthorizer("00")
                .withApprovalCode("00")
                .withAuthorizationCode("00")
                .withOriginalTransactionDate("0821")
                .withTransactionTime("090520")
                .withAuthorizer(AuthorizerCode.Interchange_Authorized)
                .build();

        transaction.setOrigionalAmount(new BigDecimal(10));
        TransactionReference reference = new TransactionReference();
        reference.setOriginalPaymentMethod(track).setOriginalTransactionCode(TransactionCode.PurchaseReturn);
        reference.setOriginalPaymentMethod(track).setOriginalAmount(new BigDecimal(10));
        reference.setOriginalTransactionTime("095550");
        reference.setOriginalTransactionDate("0729");
        transaction.setTransactionReference(reference);

        GatewayException incorrectFormat2 = assertThrows(GatewayException.class,
                () -> transaction.preAuthCompletion(new BigDecimal(10))
                        .withCurrency("USD")
                        .withNtsProductData(getProductDataForNonFleetBankCards(track))
                        .withNtsTag16(getTag16())
                        .withSettlementAmount(new BigDecimal(12))
                        .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                        .execute());
        assertNotNull(incorrectFormat2);
    }
}
