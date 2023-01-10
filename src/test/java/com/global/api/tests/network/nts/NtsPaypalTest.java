package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NtsPaypalTest {
    private CreditTrackData track;
    private NtsRequestMessageHeader header;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;

    public NtsPaypalTest() throws ConfigurationException{

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

        //NTS header
        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("510");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);
        header.setPriorMessageResponseTime(1);
        header.setPriorMessageConnectTime(999);
        header.setPriorMessageCode("01");

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
        config.setCompanyId("009");
        config.setTerminalId("01");
        config.setUnitNumber("00001234567");
        config.setSoftwareVersion("21");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        // PayPal track 2
        track = NtsTestCards.PaypalTrack2(EntryMethod.Swipe);
        track.setCardType("PayPal");
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

    /**
     * Authorization Request Format with Track 1 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_Paypal_001_Auth_Track1_Amount_Expansion() throws ApiException {

        track = NtsTestCards.PaypalTrack1(EntryMethod.Swipe);
        track.setCardType("PayPal");

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }
    /**
     * Authorization Request Format with Track 2 Data
     * and Amount Expansion
     * @throws ApiException
     */
    @Test
    public void test_PayPal_002_Auth_Track2_Amount_Expansion() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }
    /**
     * Sale Request Format with Track 1 Data
     * @throws ApiException
     */
    @Test
    public void test_Paypal_001_Sales_With_Track1() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);
        track = NtsTestCards.PaypalTrack1(EntryMethod.Swipe);
        track.setCardType("PayPal");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());
    }
    /**
     * Sale Request Format with Track 2 Data
     * @throws ApiException
     */
    @Test
    public void test_Paypal_002_Sales_With_Track2() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(response);

        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    /**
     * Preauth void  Request format
     * @throws ApiException
     */
    @Test
    public void test_Paypal_001_PreauthVoid_Credit() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Sales void Request format
     * @throws ApiException
     */
    @Test
    public void test_Paypal_002_SalesVoid_Credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }

    /**
     * Auth reversal Request format
     * @throws ApiException
     */
    @Test
    public void test_Paypal_001_AuthReversal_Credit() throws ApiException {

        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00",response.getResponseCode());

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction reversalResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        assertEquals("00", reversalResponse.getResponseCode());
    }
    /**
     * Sales reversal Request format
     */
    @Test
    public void test_Paypal_002_SaleReversal_Credit() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction reversalResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();

        assertEquals("00", reversalResponse.getResponseCode());
    }
    /**
     * Data collect Request format with User data
     */
    @Test
    public void test_PayPal_001_DataCollect_With_UserData() throws ApiException {

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction chargeResponse = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withCvn("123")
                .execute();
        assertNotNull(chargeResponse);
        assertEquals("30", chargeResponse.getResponseCode());

        // Data-Collect request preparation.
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = chargeResponse.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(track))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }

}
