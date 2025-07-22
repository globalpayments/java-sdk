package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.EbtCardType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE127_ForwardingData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsTokenizationTests {
    private CreditCardData card;
    private CreditTrackData track;
    private AcceptorConfig acceptorConfig;

    public VapsTokenizationTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

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
        acceptorConfig.setVisaFleet2(false);

        acceptorConfig.setServiceType(ServiceType.GPN_API);
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.DeTokenize);
        acceptorConfig.setTokenizationType(TokenizationType.MerchantTokenization);
        acceptorConfig.setMerchantId("00009121977");
        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0007998855611");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // VISA
//        card = TestCards.VisaManual(true, true);
//        cashCard = TestCards.VisaSwipe();

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
    public void test_MasterCard_file_action() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("5473500000000014");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Visa_file_action() throws ApiException {
        card = TestCards.VisaManual(true, true);
        card.setTokenizationData("4012002000060016");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Discover_file_action() throws ApiException {
        card = TestCards.DiscoverManual();
        card.setTokenizationData("6011000990156527");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_Amex_file_action() throws ApiException {
        card = TestCards.AmexManual();
        card.setTokenizationData("372700699251018");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_file_action() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("5506740000004316");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_file_action_track() throws ApiException {
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("%B5473500000000014^MC TEST CARD^251210199998888777766665555444433332?;5473500000000014=25121019999888877776?");
        Transaction response = track.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_001_credit_manual_auth() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_credit_manual_sale() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void test_016_authCapture() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.authorize(new BigDecimal(10), true)
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
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_004_credit_refund() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_credit_balance_inquiry() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_sale_Mastercard_Partial_reversal() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);

        Transaction response = card.charge(new BigDecimal(50))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        response.setNtsData(ntsData);
        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withPartialApproval(true)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_sale_Discover_reversal() throws ApiException {
        card = TestCards.DiscoverManual();
        card.setTokenizationData("EE4341ED58284F24E971972F758D19D5F75661FC463A0EF6C68270E68473E733D80261E7A36C9C946C73DF8DA6825F3A5A15B30CCD9327F8EF31354DBBF5B6E8");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_sale_Amex_reversal() throws ApiException {
        card = TestCards.AmexManual();
        card.setTokenizationData("D085A20177954A813CBF23822553861C540CE0DF75F72E98EAF3BDB728EA3CDA7A01C5E407F494853019363DB8F1745682C067936E174CE73D7313706AAE4425");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_sale_Visa_reversal() throws ApiException {
        card = TestCards.VisaManual();
        card.setTokenizationData("5D9BC5BC32F314973B49A16B20AE0555893D67243D917BF2A31E55202A3FBF8EE5A69BE2EACED3795D0FE477554AF45AB4DD821D09FA6D73E1B3ADF991DD5196");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    //void of partial approval
    @Test
    public void test_000_credit_Mastercard_Void_partial_approval() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("32340A64B2A3CD4B4A7A5D3D73AAB7EDF8D494E2DEF475C1B2FB3E375DA83BA06DF4F1BB4A2DB70E74351761806D21757EA653F9D70E845073A251AE58FE2F70");

        Transaction response = card.charge(new BigDecimal(100))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        System.out.println("authorizedAmount - " + authorizedAmount);
        assertNotEquals(new BigDecimal("142"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .withReferenceNumber(response.getReferenceNumber())
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        // check message data
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4353", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_015_credit_void() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction
        Transaction reverseResponse = response.voidTransaction().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test // abandoned Authorization
    public void test_000_credit_Visa_abandoned_authorization() throws ApiException {
        card = TestCards.VisaManual();
        card.setTokenizationData("5D9BC5BC32F314973B49A16B20AE0555893D67243D917BF2A31E55202A3FBF8EE5A69BE2EACED3795D0FE477554AF45AB4DD821D09FA6D73E1B3ADF991DD5196");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFee(FeeType.TransactionFee,new BigDecimal(1))
                .execute();

        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // partial approval cancellation
        Transaction reversal = response.cancel()
                .withReferenceNumber(response.getReferenceNumber())
                .execute();
        assertNotNull(reversal);

        pmi = reversal.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals(reversal.getResponseMessage(), "400", reversal.getResponseCode());
    }

    @Test // abandoned Authorization
    public void test_000_credit_Visa_void_Full() throws ApiException {
        card = TestCards.VisaManual();
        card.setTokenizationData("5D9BC5BC32F314973B49A16B20AE0555893D67243D917BF2A31E55202A3FBF8EE5A69BE2EACED3795D0FE477554AF45AB4DD821D09FA6D73E1B3ADF991DD5196");

            Transaction auth = card.authorize(new BigDecimal(12))
                    .withCurrency("USD")
                    .execute();
            assertNotNull(auth);
            assertEquals("000", auth.getResponseCode());

            ManagementBuilder builder = auth.voidTransaction()
                    .withForceToHost(true);

            HashMap<CardIssuerEntryTag, String> issuerData = auth.getIssuerData();
            for(CardIssuerEntryTag key: issuerData.keySet()) {
                builder.withIssuerData(key, issuerData.get(key));
            }

            Transaction response = builder.execute();
            assertNotNull(response);

            // check message data
            PriorMessageInformation pmi = response.getMessageInformation();
            assertNotNull(pmi);
            assertEquals("1420", pmi.getMessageTransactionIndicator());
            assertEquals("003000", pmi.getProcessingCode());
            assertEquals("441", pmi.getFunctionCode());
            assertEquals("4356", pmi.getMessageReasonCode());

            // check response
            assertEquals("400", response.getResponseCode());
        }

    @Test
    public void test_015_Amex_void() throws ApiException {
        card = TestCards.AmexManual();
        card.setTokenizationData("D085A20177954A813CBF23822553861C540CE0DF75F72E98EAF3BDB728EA3CDA7A01C5E407F494853019363DB8F1745682C067936E174CE73D7313706AAE4425");

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction
        Transaction reverseResponse = response.voidTransaction().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    //mastercard purchasing
    @Test
    public void test_file_action_mastercard_purchasing() throws ApiException {
        card = TestCards.MasterCardPurchasingManual();
        card.setTokenizationData("5302490000004066");
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }


    @Test
    public void test_001_credit_swipe_auth() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = track.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_016_authCapture_swipe() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        Transaction response = track.authorize(new BigDecimal(10), true)
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
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(response.getAuthorizedAmount())
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_002_credit_sale_swipe() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void test_004_credit_swipe_refund() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = track.refund(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_005_credit_balance_inquiry_swipe() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = track.balanceInquiry()
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("303000", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_sale_reversal_swipe() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        //assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("003000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }
    @Test
    public void test_015_credit_void_swipe() throws ApiException {
        track = TestCards.MasterCardSwipe();
        track.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        // reverse the transaction
        Transaction reverseResponse = response.voidTransaction().execute();
        assertNotNull(reverseResponse);
        assertEquals(response.getResponseMessage(), "400", reverseResponse.getResponseCode());
    }

    @Test
    public void credit_return() throws ApiException {
        card = TestCards.VisaManual(true, true);
        card.setTokenizationData("5D9BC5BC32F314973B49A16B20AE0555893D67243D917BF2A31E55202A3FBF8EE5A69BE2EACED3795D0FE477554AF45AB4DD821D09FA6D73E1B3ADF991DD5196");
        Transaction response = card.refund(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    //EBT
    @Test
    public void test_file_action_Ebt() throws ApiException {
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        EBTTrackData cashCard ;
        cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        cashCard.setPinBlock("62968D2481D231E1A504010024A00014");
        cashCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
        cashCard.setEncryptedPan("4355567063338");

        cashCard.setTokenizationData("4355567063338");
        Transaction response = cashCard.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void test_002_EBT_sale_swipe() throws ApiException {
        EBTTrackData cashCard ;
        cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        cashCard.setPinBlock("62968D2481D231E1A504010024A00014");
        cashCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
        cashCard.setEncryptedPan("4355567063338");
        cashCard.setTokenizationData("C2A530C7B4651E5D08C48ED1973F0E3D0D94AE13E41A573A521DCC0BCE69341C820274C2EAF82D9B50CD4CA87EC623454E01607E56CD4278B537C06EB75DD44A");

        Transaction response = cashCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void EBT_balance_inquiry() throws ApiException {
        EBTTrackData cashCard = new EBTTrackData(EbtCardType.CashBenefit);
        cashCard.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        cashCard.setPinBlock("62968D2481D231E1A504010024A00014");
        cashCard.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));
        cashCard.setEncryptedPan("4355567063338");
        cashCard.setTokenizationData("C2A530C7B4651E5D08C48ED1973F0E3D0D94AE13E41A573A521DCC0BCE69341C820274C2EAF82D9B50CD4CA87EC623454E01607E56CD4278B537C06EB75DD44A");
        Transaction response = cashCard.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    //GIFT Card
    @Test
    public void test_gift_file_action_track() throws ApiException {
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("6010561234567890123=25010004000070779628");
        Transaction response = giftCard.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_001_gift_swipe_auth() throws ApiException {
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("4726B8BAA540EA4CDAAE92AC97F9F89AC81F9B1337066C6186AF6E39E429B443E95BDD5577128334C04A6DF3D2429A64AFDE9F202FF15E5A2BBDDE28DBEA81B6");
        Transaction response = giftCard.authorize(new BigDecimal(10),true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_002_gift_sale_swipe() throws ApiException {
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("4726B8BAA540EA4CDAAE92AC97F9F89AC81F9B1337066C6186AF6E39E429B443E95BDD5577128334C04A6DF3D2429A64AFDE9F202FF15E5A2BBDDE28DBEA81B6");
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void giftCard_auth_capture() throws ApiException {
        GiftCard giftCard = TestCards.GiftCard1Swipe();
        giftCard.setTokenizationData("68E86F83E9DB786188C9BBDEF5012205A176EDDF238DC9E2B0C0D5974C0050CFEF90CC57D4D21EF97090CBB2CE87E7CE9D6B2E5FD1A4FEEBA7BCE112E2489C5F");
        Transaction response = giftCard.authorize(new BigDecimal(50), true)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(50),
                response.getAuthorizationCode(),
                response.getNtsData(),
                giftCard,
                response.getMessageTypeIndicator(),
                response.getSystemTraceAuditNumber(),
                response.getOriginalTransactionTime()
        );

        Transaction captureResponse = transaction.capture(new BigDecimal(35.24))
                .withCurrency("USD")
                .execute();
        assertNotNull(captureResponse);
        assertEquals(captureResponse.getResponseMessage(), "000", captureResponse.getResponseCode());
    }

    @Test
    public void giftCard_balance_inquiry() throws ApiException {
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("4726B8BAA540EA4CDAAE92AC97F9F89AC81F9B1337066C6186AF6E39E429B443E95BDD5577128334C04A6DF3D2429A64AFDE9F202FF15E5A2BBDDE28DBEA81B6");
        Transaction response = giftCard.balanceInquiry()
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_return() throws ApiException {
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("4726B8BAA540EA4CDAAE92AC97F9F89AC81F9B1337066C6186AF6E39E429B443E95BDD5577128334C04A6DF3D2429A64AFDE9F202FF15E5A2BBDDE28DBEA81B6");
        Transaction response = giftCard.refund(new BigDecimal(35.24))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void giftCard_void() throws ApiException {
        GiftCard giftCard = TestCards.GiftCard1Swipe();
        giftCard.setTokenizationData("68E86F83E9DB786188C9BBDEF5012205A176EDDF238DC9E2B0C0D5974C0050CFEF90CC57D4D21EF97090CBB2CE87E7CE9D6B2E5FD1A4FEEBA7BCE112E2489C5F");
        Transaction response = giftCard.charge(new BigDecimal(10.00))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("000", response.getResponseCode());

        Transaction voidResponse = response.voidTransaction().execute();
        assertNotNull(voidResponse);
        assertEquals("000", voidResponse.getResponseCode());
    }

    @Test
    public void test_gift_sale_reversal_swipe() throws ApiException {
        GiftCard giftCard = TestCards.ValueLinkSwipe();
        giftCard.setTokenizationData("4726B8BAA540EA4CDAAE92AC97F9F89AC81F9B1337066C6186AF6E39E429B443E95BDD5577128334C04A6DF3D2429A64AFDE9F202FF15E5A2BBDDE28DBEA81B6");

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);
        Transaction response = giftCard.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("006000", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("006000", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void tests_DE127_ForwardingData_parsing() {
        String original = "01TOK216TD001G21100200  00009121977                     D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526                                        ";

        DE127_ForwardingData element = new DE127_ForwardingData().fromByteArray(original.getBytes());

        assertNotNull(element.toByteArray());
    }

    // DE 127 response field validation
    @Test
    public void test_DE127_TokenEntryData_ServiceResponseField_Validation() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("5473500000000014");
        acceptorConfig.setTokenizationOperationType(TokenizationOperationType.Tokenize);
        Transaction response = card.fileAction()
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    @Test
    public void test_DE127_TokenEntryData_DeTokenize() throws ApiException {
        card = TestCards.MasterCardManual();
        card.setTokenizationData("D7EB9013AD6E5962440994A6155221446250CC77A23ECD6DBAD5506337DB7C0E3A4BF39D97450709EE7D2C2FBB547E42ADC851E0B29ABC2DBB4EC4E07CC26526");
        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
}
