package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayTimeoutException;
import com.global.api.network.elements.DE117_WIC_Data_Field_EA;
import com.global.api.network.elements.DE117_WIC_Data_Field_PS;
import com.global.api.network.elements.EWICData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.network.enums.gnap.PINCapability;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.EwicCardData;
import com.global.api.paymentMethods.EwicTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class NWSEwicTests {
    private EwicCardData card;
    private EwicTrackData track;
    private EWICData ewicData;
    private Address address;
    public NWSEwicTests() throws ApiException {
        address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardCaptureCapability(false);
        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Attended);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.NotAuthenticated);
        acceptorConfig.setCardDataOutputCapability(CardDataOutputCapability.Unknown);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setPinCapability(PINCapability.PINEntryCapable);
        acceptorConfig.setPinCaptureCapability(PinCaptureCapability.TwelveCharacters);



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
        NetworkGatewayConfig config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns-e.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5541");
        config.setEwicMerchantId("123456789");
        ServicesContainer.configureService(config);


        track= new EwicTrackData();
        track.setValue("6103189999904638=49121200000364?");
        track.setPinBlock("12348D2481D231E1A504010024A00014");
        track.setEntryMethod(EntryMethod.Swipe);

        card=new EwicCardData();
        card.setNumber("6103189999903663");
        card.setExpMonth(12);
        card.setExpYear(2050);
        card.setPinBlock("12348D2481D231E1A504010024A00014");

        // use either EA data or PS data while running test cases...
        ewicData= new EWICData();
        DE117_WIC_Data_Field_EA eaData = new DE117_WIC_Data_Field_EA();
        eaData.setUpcData("11110583000");
        eaData.setCategoryCode("2");
        eaData.setSubCategoryCode("2");
        eaData.setBenefitQuantity("500");
        eaData.setItemDescription("desc");
        // ewicData.add(eaData);

        DE117_WIC_Data_Field_PS epData = new DE117_WIC_Data_Field_PS();
        epData.setUpcData("123456789012");
        epData.setItemPrice("100");
        epData.setPurchaseQuantity("1500");
        epData.setItemActionCode("12");
        ewicData.add(epData);
    }
    @Test
    public void test_001_swipe_sale() throws ApiException {
        DE117_WIC_Data_Field_EA eaData = new DE117_WIC_Data_Field_EA();
        eaData.setUpcData("11110583000");
        eaData.setCategoryCode("2");
        eaData.setSubCategoryCode("2");
        eaData.setBenefitQuantity("500");
        eaData.setItemDescription("desc");
        ewicData.add(eaData);
        Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_002_balance_enquiry() throws ApiException {
        Transaction response = track.balanceInquiry()
                .withEWICData(ewicData)
                .WithEWICIssuingEntity("1122334455")
                .withAddress(address, AddressType.Billing)
                .withClerkId("41256")
                .execute();
        assertNotNull(response);
        System.out.println(ewicData.toString());

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("319700", pmi.getProcessingCode());
        assertEquals("108", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void Test_003_merchant_initiated_swipe_void_sale() throws ApiException {
     Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withClerkId("41256")
                .withAddress(address, AddressType.Billing)
                .withCurrency("USD")
                .execute();

     assertNotNull(response);

     Transaction voidResponse = response.voidTransaction().withClientTransactionId(response.getReferenceNumber()).execute();

     assertNotNull(response);
     PriorMessageInformation pmi= voidResponse.getMessageInformation();
     assertEquals("1420", pmi.getMessageTransactionIndicator());
     assertEquals("009700", pmi.getProcessingCode());
     assertEquals("441", pmi.getFunctionCode());
     assertEquals("4351", pmi.getMessageReasonCode());

     assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void Test_004_customer_initiated_swipe_void_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);

        Transaction voidResponse = response
                .voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        assertNotNull(response);
        PriorMessageInformation pmi= voidResponse.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        assertEquals("400", voidResponse.getResponseCode());
    }

    @Test
    public void test_005_manual_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withClerkId("41256")
                .withAddress(address, AddressType.Billing)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void Test_006_manual_merchant_initiated_swipe_void_sale() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withClerkId("41256")
                .withAddress(address, AddressType.Billing)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);

        Transaction voidResponse = response.voidTransaction().execute();

        assertNotNull(response);
        PriorMessageInformation pmi= voidResponse.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test
    public void Test_007_manual_customer_initiated_swipe_void_sale() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withClerkId("41256")
                .withAddress(address, AddressType.Billing)
                .withCurrency("USD")
                .execute();

        assertNotNull(response);

        Transaction voidResponse = response
                .voidTransaction()
                .withCustomerInitiated(true)
                .execute();

        assertNotNull(response);
        PriorMessageInformation pmi= voidResponse.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        assertEquals("400", voidResponse.getResponseCode());
    }
    @Test(expected = GatewayTimeoutException.class)
    public void test_008_swipe_reverse_sale() throws ApiException {
        track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withSimulatedHostErrors(Host.Primary, HostError.Timeout)
                .withSimulatedHostErrors(Host.Secondary, HostError.Timeout)
                .execute();
    }

    @Test
    public void test_005_manual_sale_reversal() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withClerkId("41256")
                .withAddress(address, AddressType.Billing)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        NtsData ntsData =  new NtsData(FallbackCode.Received_IssuerUnavailable, AuthorizerCode.Terminal_Authorized);
        response.setNtsData(ntsData);

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(10))
                .withCurrency("USD")
                .withClerkId("41256")
                .withClientTransactionId(response.getReferenceNumber())
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }


    @Test
    public void test_009_swipe_sale_reversal() throws ApiException {
        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = track.charge(new BigDecimal(10))
                .withEWICData(ewicData)
                .withCurrency("USD")
                .execute();
        assertNotNull(response);

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
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
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_011_swipe_void() throws ApiException {
        Transaction sale = track.charge(new BigDecimal(12))
                .withEWICData(ewicData)
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("210060", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    @Test
    public void test_store_and_forward() throws ApiException {
     Transaction response = track.storeAndForward(new BigDecimal(10))
                .withCurrency("USD")
                .withClerkId("41256")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("009700", pmi.getProcessingCode());
        assertEquals("208", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());



    }

}
