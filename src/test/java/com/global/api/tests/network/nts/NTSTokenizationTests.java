package com.global.api.tests.network.nts;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.entities.NtsProductData;
import com.global.api.network.entities.NtsTag16;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.nts.NtsRequestMessageHeader;
import com.global.api.network.entities.nts.NtsUtilityMessageRequest;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.NetworkService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.NtsTestCards;
import com.global.api.tests.testdata.TestCards;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NTSTokenizationTests {
    private CreditCardData card;
    private NtsRequestMessageHeader header; //Main Request header class
    private NtsTag16 tag;
    private AcceptorConfig acceptorConfig;
    private NetworkGatewayConfig config;
    private NtsProductData productData;
    private PriorMessageInformation priorMessageInformation;
    Integer Stan = Integer.parseInt(DateTime.now().toString("hhmmss"));
    public NTSTokenizationTests() throws ConfigurationException {

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
        acceptorConfig.setPosActionCode(true);
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        //tokenization configuration
        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        acceptorConfig.setTokenizationType(TokenizationType.MerchantTokenization);
        acceptorConfig.setMerchantId("00066654534");

        header = new NtsRequestMessageHeader();
        header.setTerminalDestinationTag("999");
        header.setPinIndicator(PinIndicator.NotPromptedPin);
        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        priorMessageInformation =new PriorMessageInformation();
        priorMessageInformation.setResponseTime("1");
        priorMessageInformation.setConnectTime("999");
        priorMessageInformation.setMessageReasonCode("01");

        header.setPriorMessageInformation(priorMessageInformation);

        tag = new NtsTag16();
        tag.setPumpNumber(1);
        tag.setWorkstationId(1);
        tag.setServiceCode(ServiceCode.Self);
        tag.setSecurityData(SecurityData.NoAVSAndNoCVN);

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
        config.setInputCapabilityCode(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        config.setTerminalId("21");
        config.setUnitNumber("00066654534");
        config.setSoftwareVersion("01");
        //config.setCompanyId("045");
        config.setLogicProcessFlag(LogicProcessFlag.Capable);
        config.setTerminalType(TerminalType.VerifoneRuby2Ci);
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);


    }

    private NtsProductData getProductDataForNonFleetBankCards(IPaymentMethod method) throws ApiException {
        productData = new NtsProductData(ServiceLevel.FullServe, method);
        productData.addFuel(NtsProductCode.Diesel1, UnitOfMeasure.Gallons,1.24, 2.899);
        productData.addFuel(NtsProductCode.Diesel2, UnitOfMeasure.Gallons,2.24, 1.259);
        productData.addNonFuel(NtsProductCode.Batteries,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.CarWash,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Dairy,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Candy,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.Milk,UnitOfMeasure.NoFuelPurchased,1,1.74);
        productData.addNonFuel(NtsProductCode.OilChange,UnitOfMeasure.NoFuelPurchased,1,10.74);
        productData.setPurchaseType(PurchaseType.FuelAndNonFuel);
        productData.add(new BigDecimal("32.33"),new BigDecimal(0));
        return productData;
    }

    @Test //working
    public void test_Nts_Utility_Message_credit_mastercard_tokenization() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("5473500000000014");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setTokenizationData("5473500000000014");

        ntsUtilityMessageRequest.setICardData(cardData);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_Nts_Utility_Message_credit_visa_tokenization() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("4012002000060016");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setTokenizationData("4012002000060016");

        ntsUtilityMessageRequest.setICardData(cardData);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }  @Test //working
    public void test_Nts_Utility_Message_credit_amex_tokenization() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("372700699251018");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);
        cardData.setTokenizationData("372700699251018");

        ntsUtilityMessageRequest.setICardData(cardData);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }

    //------------------------prepaid tokenization --------------------------------//

    @Test
    public void test_Nts_Utility_Message_prepaid__tokenization() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        GiftCard giftCard = new GiftCard();
        giftCard.setExpiry("1225");
        giftCard.setCardType("StoredValue");
        giftCard.setTokenizationData("6006491286999911672");
        ntsUtilityMessageRequest.setGiftCard(giftCard);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }


    // ---------------------------Delete token ------------------------------------//
    @Test //working
    public void test_Nts_Utility_Message_credit_mastercard_delete_token() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeleteToken);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.UtilityMessage);

        NtsUtilityMessageRequest ntsUtilityMessageRequest = new NtsUtilityMessageRequest();
        ntsUtilityMessageRequest.setUtilityType(002);
        ntsUtilityMessageRequest.setReserved("");

        CreditCardData cardData = new CreditCardData();
        cardData.setNumber("5473500000000014");
        cardData.setExpMonth(12);
        cardData.setExpYear(2025);

        ntsUtilityMessageRequest.setICardData(cardData);

        Transaction response = NetworkService.sendUtilityMessage()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsUtilityMessageRequest(ntsUtilityMessageRequest)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }

    //-------------------------- Mastercard detokenization --------------------------//

    @Test
    public void test_001_sales_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_authorize_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_balance_inquiry_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");

        Transaction response = card.balanceInquiry(InquiryType.Cash)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withNtsProductData(productData)
                //.withNtsTag16(tag)
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_auth_capture_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                //.withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_004_refund_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // refund request
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());
    }
    @Test
    public void test_void_transaction_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_reversal_without_track_mastercard() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);
        card.setCardType("MC");
        card.setTokenizationData("43840500F2BB20EE4456EEF73060859B424A5D3EC8EB612FF6964DCACF8DBDA03C44CC86568FC71B3BB2D0273AFF97686C785462E53F9543D005B2E1FC5D5B90");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    // ------------------------------------ Visa DeTokenization -------------------------------//
    @Test
    public void test_001_sales_without_track_visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_balanceInquiry_without_track_visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");

        Transaction response = card.balanceInquiry()
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withAmount(new BigDecimal(0))
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_authorize_without_track_visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_auth_capture_without_track_Visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_004_refund_without_track_Visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // refund request
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());
    }
    @Test
    public void test_void_transaction_without_track_Visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_reversal_without_track_Visa() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = new CreditCardData();
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCardType("Visa");
        card.setTokenizationData("243C9E8ED80AA80F9FE36390F0E1A811B6BEC1E57F0415BAB54AA7200E18FC3CD39F92508CEDC7D8BB9A444E4B5092473EEC7BF7E0BA8E714951F078A5071131");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }


    //-------------------------------------- Amex DeTokenization ------------------------------------//
    @Test
    public void test_001_sales_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = TestCards.AmexManual();
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withNtsProductData(productData)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }
    @Test
    public void test_002_authorize_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = TestCards.AmexManual(true, true);
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_003_auth_capture_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.AuthorizationOrBalanceInquiry);

        card = TestCards.AmexManual(true, true);
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");
        productData = getProductDataForNonFleetBankCards(card);

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                 .withNtsTag16(tag)
                .withCvn("123")
                .execute();

        assertNotNull(response);

        // check response
        assertEquals("00", response.getResponseCode());

        // Data-Collect request preparation.
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        Transaction dataCollectResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(dataCollectResponse);

        // check response
        assertEquals("00", dataCollectResponse.getResponseCode());
    }
    @Test
    public void test_004_refund_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        header.setNtsMessageCode(NtsMessageCode.DataCollectOrSale);

        card = TestCards.AmexManual(true, true);
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        // refund request
        header.setPinIndicator(PinIndicator.WithoutPin);
        header.setNtsMessageCode(NtsMessageCode.CreditAdjustment);

        Transaction refundResponse = response.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsProductData(getProductDataForNonFleetBankCards(card))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();
        assertNotNull(refundResponse);
        // check response
        assertEquals("00", refundResponse.getResponseCode());
    }
    @Test
    public void test_void_transaction_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = TestCards.AmexManual(true, true);
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withGoodsSold("1000")
                .withCvn("123")
                .execute();

        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withNtsTag16(tag)
                .execute();

        assertEquals("00", voidResponse.getResponseCode());
    }
    @Test
    public void test_reversal_without_track_Amex() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        card = TestCards.AmexManual(true, true);
        card.setTokenizationData("083D89E75F0BB0D86A1D72B3EF5838C30216D7A70CF34BF0E6F0965D6212A6E21B9436E5C62C095B29913B330BCDA3EAACDCE107601DF32CE8A5AAE4F2BC2169");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withUniqueDeviceId("0102")
                .withNtsTag16(tag)
                .withCvn("123")
                .execute();
        assertNotNull(response);

        header.setNtsMessageCode(NtsMessageCode.ReversalOrVoid);

        Transaction voidResponse = response.reverse(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .execute();
        assertEquals("00", voidResponse.getResponseCode());
    }

    //-----------------------------------Prepaid detokenize ------------------------------------//

    /// prepaid card tokenization for future support

    @Test
    public void test_prepaid_svs_authorize_deTokenize() throws ApiException {

        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Attended);
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);


        GiftCard giftCard = new GiftCard();
        giftCard.setCardType("StoredValue");
        giftCard.setPin("1422");
        giftCard.setTokenizationData("FC75739DACB9508BC85804D7084835A85B8F5276CC595746564EB5B088DE88F6E6751EAC43D3F4E3D0E180850DB01FD08576523FEDA0953B06DE683A0C635B83");

        Transaction response = giftCard.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test
    public void test_prepaid_svs_sale_deTokenize() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.UnattendedAfd);

        config.setAcceptorConfig(acceptorConfig);

        GiftCard giftCard = new GiftCard();
        giftCard.setValue("B6006491260550251182^SVSMC^711211006H");
        giftCard.setPin("1422");
        giftCard.setTokenizationData("F1D405DC6809606389F6E2419D4BDF0A");

        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }

    @Test//
    public void test_prepaid_svs_return_deTokenize() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        GiftCard giftCard = new GiftCard();
        giftCard.setValue("B6006491260550251182^SVSMC^711211006H");
        giftCard.setPin("1422");
        giftCard.setTokenizationData("F1D405DC6809606389F6E2419D4BDF0A");

        Transaction response = giftCard.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();


        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_prepaid_svs_balance_inquiry_deTokenize() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        GiftCard giftCard = new GiftCard();
        giftCard.setValue("B6006491260550251182^SVSMC^711211006H");
        giftCard.setPin("1422");
        giftCard.setTokenizationData("F1D405DC6809606389F6E2419D4BDF0A");

        Transaction response = giftCard.balanceInquiry()
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void test_prepaid_svs_reverse_deTokenize() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        GiftCard giftCard = new GiftCard();
        giftCard.setValue("B6006491260550251182^SVSMC^711211006H");
        giftCard.setPin("1422");
        giftCard.setTokenizationData("F1D405DC6809606389F6E2419D4BDF0A");

        Transaction response = giftCard.charge(new BigDecimal(100))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("15", response.getResponseCode());


        response = response.reverse(new BigDecimal(100))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

    }
    @Test
    public void test_prepaid_svs_void_deTokenize() throws ApiException {

        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        config.setAcceptorConfig(acceptorConfig);

        GiftCard giftCard = new GiftCard();
        giftCard.setValue("%B6006491260550251158^SVSMC^7112110F88?");
        giftCard.setPin("1422");
        giftCard.setEntryMethod(EntryMethod.Swipe);
        giftCard.setTokenizationData("F1D405DC6809606389F6E2419D4BDF0A");

        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Stan)
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());

        response = response.voidTransaction(new BigDecimal(10))
                .withNtsRequestMessageHeader(header)
                .withSystemTraceAuditNumber(Integer.parseInt(response.getTransactionReference().getSystemTraceAuditNumber()))
                .execute();

        // check response
        assertEquals("00", response.getResponseCode());
    }
}
