package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.EBTVoucherEntryData;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.PriorMessageInfo;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsEbtTest {
    private EBTTrackData cashTrack;
    private EBTTrackData foodTrack;
    private NtsRequestMessageHeader ntsRequestMessageHeader; //Main Request header class
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private PriorMessageInformation priorMessageInformation;

    public NtsEbtTest() throws ApiException {
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
        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.Ebt);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");
        ntsRequestMessageHeader.setPriorMessageInformation(priorMessageInformation);

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

        // cash card
        cashTrack = NtsTestCards.EBTTrack2(EntryMethod.Swipe, EbtCardType.CashBenefit);

        // cash card
        foodTrack = NtsTestCards.EBTTrack2(EntryMethod.Swipe, EbtCardType.FoodStamp);
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
    public void test_Ebt_balance_food_001() throws ApiException {
        Transaction response = foodTrack.balanceInquiry(InquiryType.Foodstamp)
                .withAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Ebt_balance_cash_002() throws ApiException {
        Transaction response = cashTrack.balanceInquiry(InquiryType.Cash)
                .withAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Ebt_purchase_food_003() throws ApiException {
        Transaction response = foodTrack.charge()
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_purchase_cash_004() throws ApiException {
        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Return_005() throws ApiException {
        Transaction response = foodTrack.charge()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = foodTrack.refund()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Cash_Back_006() throws ApiException {
        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(cashTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Withdrawal_007() throws ApiException {
        Transaction response = cashTrack.benefitWithdrawal()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsProductData(getProductDataForNonFleetBankCards(cashTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Withdrawal_Reversal_009() throws ApiException {
        Transaction response = cashTrack.benefitWithdrawal()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
        Transaction reverseResponse = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCashBackAmount(new BigDecimal(3))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(reverseResponse);

        // check response
        assertEquals("00", reverseResponse.getResponseCode());


        // Data-Collect request preparation.
        

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(cashTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_purchase_food_without_Track_010() throws ApiException {
        foodTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = foodTrack.charge()
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Return_without_Track_011() throws ApiException {
        foodTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = foodTrack.refund()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Reversal_without_Track_012() throws ApiException {
        foodTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = foodTrack.charge()
                .withAmount(new BigDecimal(1000))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Return_Reversal_without_Track_013() throws ApiException {
        foodTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = foodTrack.refund()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Ebt_purchase_cash_without_Track_014() throws ApiException {
        cashTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Cash_Back_cash_without_Track_015() throws ApiException {
        cashTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsProductData(getProductDataForNonFleetBankCards(cashTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Withdrawal_cash_without_Track_016() throws ApiException {

        cashTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = cashTrack.benefitWithdrawal()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
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
                .withNtsProductData(getProductDataForNonFleetBankCards(cashTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Reversal_without_Track_017() throws ApiException {
        cashTrack.setEntryMethod(EntryMethod.Swipe);
        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_Ebt_Purchase_Cash_Back_Reversal_cash_without_Track_018() throws ApiException {
        cashTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = cashTrack.charge()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());


        response = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        // check response
        assertEquals("00", response.getResponseCode());
    }


    @Test
    public void test_Ebt_Withdrawal_Reversal_cash_without_Track_019() throws ApiException {

        cashTrack.setEntryMethod(EntryMethod.Swipe);

        Transaction response = cashTrack.benefitWithdrawal()
                .withAmount(new BigDecimal(10))
                .withCashBack(new BigDecimal(3))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());


        response = response.reverse()
                .withAmount(new BigDecimal(10))
                .withCashBackAmount(new BigDecimal(3))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);
        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_Ebt_purchase_voucher_without_Track_020() throws ApiException {
        EBTCardData card = NtsTestCards.getFoodCardManual();

        EBTVoucherEntryData data = new EBTVoucherEntryData();
        data.setOriginalTransactionDate(DateTime.now().toString("MMdd"));
        data.setVoucherNBR("s54d5c85d7vf4dd");
        data.setTelephoneAuthCode("sdfcsd");

        Transaction response = card.charge()
                .withModifier(TransactionModifier.Voucher)
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withVoucherEntryData(data)
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
                .withVoucherEntryData(data)
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

    @Test
    public void test_Ebt_purchase_voucher_Return_without_Track_021() throws ApiException {
        EBTCardData card = NtsTestCards.getFoodCardManual();

        EBTVoucherEntryData data = new EBTVoucherEntryData();
        data.setOriginalTransactionDate(DateTime.now().toString("MMdd"));
        data.setVoucherNBR("s54d5c85d7vf4dd");
        data.setTelephoneAuthCode("sdfcsd");

        Transaction response = card.charge()
                .withModifier(TransactionModifier.Voucher)
                .withAmount(new BigDecimal(10))
                .withSurchargeAmount(new BigDecimal(1))
                .withCurrency("USD")
                .withVoucherEntryData(data)
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        String transactionDate = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionDate();
        String transactionTime = response.getNtsResponse().getNtsResponseMessageHeader().getTransactionTime();

        // Data-Collect request preparation.

        ntsRequestMessageHeader.setPinIndicator(PinIndicator.WithoutPin);
        ntsRequestMessageHeader.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(foodTrack))
                .withNtsRequestMessageHeader(ntsRequestMessageHeader)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
}
