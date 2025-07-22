package com.global.api.tests.network.vaps.Certification.SVS_HGC_3DES_Ecom_Valuelink_23point1;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AddressType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.ConfigurationException;
import com.global.api.network.elements.DE62_IME_EcommerceData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VapsEcomTests {
    private CreditCardData card;
    private CreditTrackData track;
    private AcceptorConfig acceptorConfig;
    public VapsEcomTests() throws ConfigurationException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setType(AddressType.Shipping);
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.Manual);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.None);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.AuthorizingAgent);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Unknown);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.Internet_With_SSL);
        acceptorConfig.setCardDataInputMode(DE22_CardDataInputMode.Ecommerce);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.None);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportVisaFleet2dot0(PurchaseType.Fuel);


        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0009");
        config.setTerminalId("0001237891101");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        // VISA
        card = TestCards.VisaManual(true, true);
        track = TestCards.VisaSwipe();

        // VISA CORPORATE
//        card = TestCards.VisaCorporateManual(true, true);
//        cashCard = TestCards.VisaCorporateSwipe();

        // VISA PURCHASING
//        card = TestCards.VisaPurchasingManual(true, true);
//        cashCard = TestCards.VisaPurchasingSwipe();

        // MASTERCARD
//        card = TestCards.MasterCardManual(true, true);
//        track = TestCards.MasterCardSwipe();

        // MASTERCARD PURCHASING
//        card = TestCards.MasterCardPurchasingManual(true, true);
//        cashCard = TestCards.MasterCardPurchasingSwipe();

        // AMEX
//        card = TestCards.AmexManual(true, true);
//        cashCard = TestCards.AmexSwipe();

        // DISCOVER
//        card = TestCards.DiscoverManual(true, true);
//        cashCard = TestCards.DiscoverSwipe();
    }
    @Test
    public void test_02_visa_authorization_ecommerce() throws ApiException {

        // W10101T0414C
        // 100S01T03100
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_02_visa_authorization_ecommerce_with_cavv_data() throws ApiException {

        // W10101T0414C
        // 100S01T03100
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
              //  .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_05_visa_auth_capture_ecommerce() throws ApiException {

        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        response.setNtsData(new NtsData());
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_8_visa_sale_ecommerce() throws ApiException {
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_8_visa_sale_ecommerce_with_cavv_data() throws ApiException {
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_8_visa_sale_ecommerce_with_avs() throws ApiException {
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_11_visa_auth_cancel_void() throws ApiException {

        card = TestCards.VisaManual(true,true);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());

        Transaction cancel = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = cancel.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(cancel.getResponseMessage(), "400", cancel.getResponseCode());
    }

    @Test
    public void test_12_visa_sale_void_ecomm_cavv() throws ApiException {
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        card = TestCards.VisaManual(true,true);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
           //     .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction voidTransaction = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_14_visa_sale_reverse_ecomm() throws ApiException {
        card = TestCards.VisaManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction voidTransaction = response.reverse()
                .withForceToHost(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("1381", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_02_mastercard_authorization_ecommerce() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        card = TestCards.MasterCardManual(true,true);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_mastercard_authorization_ecom_avs() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        card = TestCards.MasterCardManual(true,true);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_mastercard_authorization_ecom_cavv() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        card = TestCards.MasterCardManual(true,true);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_05_mastercard_authCapture_ecommerce() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.456),true)
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        response.setNtsData(new NtsData());
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_08_mastercard_sale_ecommerce() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_08_mastercard_sale_ecommerce_cavv() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_08_mastercard_sale_ecommerce_with_avs() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_11_mastercard_auth_cancel_ecom() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual();
        Transaction response = card.authorize(new BigDecimal(11.22),true)
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction voidTransaction = response.voidTransaction()
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_12_mastercard_sale_void_ecom() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.456))
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction voidTransaction = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_14_mastercard_sale_reverse_ecom() throws ApiException {
        DE62_IME_EcommerceData ecommerceData = new DE62_IME_EcommerceData();
        ecommerceData.setDe62ImeSubfield1(DE62_IME_Subfield1.Val_210);

        card = TestCards.MasterCardManual();
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withMasterCardDSRPCryptogram("45AB3994839NFDN930203N3N4B5B3J4N")
                .withMasterCard3DSCryptogram("5TYD7GHN94H0J0F36N9H5C5D3L03BJ0N")
                .withMasterCardEcommIndicatorsData(ecommerceData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction voidTransaction = response.reverse()
                .withForceToHost(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("1381", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }
    @Test
    public void test_02_discover_authorization_ecommerce() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_discover_authorization_ecommerce_cavv() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_discover_authorization_ecommerce_avs() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_05_discover_auth_capture_ecommerce() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        response.setNtsData(new NtsData());
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_08_discover_sale_ecommerce_with_cavv() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_08_discover_sale_ecommerce_with_avs() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_11_discover_auth_cancel_ecommerce() throws ApiException {
        card = TestCards.DiscoverManual();
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction voidTransaction = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_12_discover_sale_void_ecommerce() throws ApiException {
        card = TestCards.DiscoverManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
             //   .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction voidTransaction = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_14_discover_sale_reverse_ecommerce() throws ApiException {
        card = TestCards.DiscoverManual();
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction reverse = response.reverse()
                .withForceToHost(true)
                .execute();

        pmi = reverse.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("1381", pmi.getMessageReasonCode());
        assertEquals(reverse.getResponseMessage(), "400", reverse.getResponseCode());
    }

    @Test
    public void test_02_amex_authorization_ecommerce() throws ApiException {
        card = TestCards.AmexManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.22),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_amex_authorization_ecommerce_cavv() throws ApiException {
        card = TestCards.AmexManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.22),true)
                .withCurrency("USD")
                 .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_02_amex_authorization_ecommerce_avs() throws ApiException {
        card = TestCards.AmexManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.22),true)
                .withCurrency("USD")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_03_amex_auth_capture_ecommerce() throws ApiException {
        card = TestCards.AmexManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.authorize(new BigDecimal(10.22),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        response.setNtsData(new NtsData());
        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_06_amex_sale_ecommerce() throws ApiException {
        card = TestCards.AmexManual(true,true);;
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_06_amex_sale_ecommerce_cavv() throws ApiException {
        card = TestCards.AmexManual(true,true);;
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_06_amex_sale_ecommerce_avs() throws ApiException {
        card = TestCards.AmexManual(true,true);;
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withAvs(true)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_08_amex_auth_cancel_ecommerce() throws ApiException {
        card = TestCards.AmexManual(true,true);;
        Transaction response = card.authorize(new BigDecimal(10.11),true)
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction voidTransaction = response.voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_09_amex_sale_void_ecommerce() throws ApiException {

        card = TestCards.AmexManual(true,true);;
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
            //    .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction voidTransaction = response.voidTransaction()
                 .withCustomerInitiated(true)
                .execute();

        pmi = voidTransaction.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(voidTransaction.getResponseMessage(), "400", voidTransaction.getResponseCode());
    }

    @Test
    public void test_11_amex_sale_reverse_ecommerce() throws ApiException {

        card = TestCards.AmexManual(true,true);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        Transaction response = card.charge(new BigDecimal(10.22))
                .withCurrency("USD")
                .withCardIssueAuthenticationData("45AB3994839NFDN930203N3N4B5B3J4NO7G6T8F7")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        Transaction reverse = response.reverse()
                .withForceToHost(true)
                .execute();

        pmi = reverse.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("1381", pmi.getMessageReasonCode());
        assertEquals(reverse.getResponseMessage(), "400", reverse.getResponseCode());
    }

}
