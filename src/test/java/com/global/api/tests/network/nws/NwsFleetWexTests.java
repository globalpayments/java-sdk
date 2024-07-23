package com.global.api.tests.network.nws;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Target;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.network.entities.*;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.services.BatchService;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NwsFleetWexTests {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;
    private NetworkGatewayConfig config;
    private Address address;
    AcceptorConfig acceptorConfig;

    public NwsFleetWexTests() throws ApiException {
        acceptorConfig = new AcceptorConfig();

        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        //address
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
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
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        config = new NetworkGatewayConfig(Target.NWS);
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("SPSA");
        config.setTerminalId("NWSJAVA05");
        config.setUniqueDeviceId("0001");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        config.setMerchantType("5541");
        ServicesContainer.configureService(config);

        fleetData = new FleetData();
        fleetData.setServicePrompt("0");

        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Heartland);
        productData.add(ProductCode.Unleaded_Gas, UnitOfMeasure.Gallons, 1, 10);

    }
    @Test
    public void test_000_batch_close() throws ApiException {
        BatchSummary summary = BatchService.closeBatch();
        assertNotNull(summary);
        assertTrue(summary.isBalanced());
    }

    //Wex Authorization
    @Test
    public void test_Authorization_OTP_WX_1100() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

    }

    //Wex auth capture
    @Test
    public void test_021_swipe_AuthCapture_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config);

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46581");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121004658100000");

        Transaction preRresponse = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("05", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("45", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_sales_wex() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_sales_wex_amount_40() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(20), new BigDecimal(30), new BigDecimal(100));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withSalesTaxAdditionAmount(new BigDecimal(10))
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

    //Wex Voice Capture
    @Test
    public void test_WX_VC() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction trans = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Voice_Authorized),
                card
        );

        Transaction response = trans.capture(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

    }

    @Test
    public void test_006_swipe_void() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = card.charge(new BigDecimal(12))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());
        //assertNotNull(sale.getReferenceNumber());

        Transaction response = sale.voidTransaction()
                //  .withReferenceNumber(sale.getReferenceNumber())
                .withReferenceNumber("0000")
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", response.getResponseCode());
    }

    //Reversal
    @Test
    public void test_sale_wex_reversal() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        NtsData ntsData = new NtsData(FallbackCode.Received_IssuerUnavailable,AuthorizerCode.Terminal_Authorized);

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        response.setNtsData(ntsData);
        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(reversal);

        // check message data
        pmi = reversal.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4021", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_021_swipe_wex_AuthCancel_cards() throws ApiException {
        config.setMerchantType("5542");
        ServicesContainer.configureService(config,"ICR");

        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46571");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014657100000");

        Transaction preRresponse = card.authorize(new BigDecimal(30))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(preRresponse);

        // check message data
        PriorMessageInformation pmi = preRresponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());

        // check response
        assertEquals("000", preRresponse.getResponseCode());

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("005", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("045", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = preRresponse.cancel()
                .withReferenceNumber("0000")
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);

        pmi = response.getMessageInformation();
        assertEquals("1420", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("441", pmi.getFunctionCode());
        assertEquals("4352", pmi.getMessageReasonCode());

        assertEquals(response.getResponseMessage(), "400", response.getResponseCode());

    }

    //Voyager void of partial approval
    @Test
    public void test_000_wex_void_partial_approval() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46571");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014657100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withAllowPartialAuth(true)
                .execute();
        assertNotNull(response);
        //assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("20"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(new BigDecimal("20"))
                .withCurrency("USD")
                .withReferenceNumber("0000")
                .withCustomerInitiated(true)
                .withPartialApproval(true)
                .withFleetData(fleetData)
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
    /** DE 40 Additional Amount */
    @Test
    public void test_sales_wex_with_de_40_amount() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(20), new BigDecimal(30), new BigDecimal(100));

        Transaction response = card.charge(new BigDecimal(30))
                .withCurrency("USD")
                .withSalesTaxAdditionAmount(new BigDecimal(10))
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    public void test_003_swipe_refund_codeCoverage() throws ApiException {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46571");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014657100000");

        Transaction response = card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("200009", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    /** Exception code coverage required field missing*/
    @Test
    public void test_003_swipe_refund_codeCoverage_Excp() {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46571");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014657100000");

        BuilderException builderException = assertThrows(BuilderException.class,
                () -> card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute());
        assertEquals("Transaction mapping data object required for WEX refunds.", builderException.getMessage());
    }

    /** Exception code coverage batch number field missing*/
    @Test
    public void test_003_swipe_refund02_codeCoverage(){
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46571");
        fleetData.setServicePrompt("0");

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460430001234566=24121014657100000");

        BuilderException builderException = assertThrows(BuilderException.class,
                () -> card.refund(new BigDecimal(10))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .withTransactionMatchingData(new TransactionMatchingData(null, null))
                .execute());

        assertEquals("Transaction Matching Data incomplete. Original batch number and date are required for WEX refunds.", builderException.getMessage());
    }

    /** Null Pointer Exception code coverage */
    @Test
    public void test_sales_wex_codeCoverage_Excp() {
        fleetData.setDriverId("373395");
        fleetData.setVehicleNumber("46561");
        fleetData.setServicePrompt("0");
        fleetData.setPurchaseDeviceSequenceNumber(null);

        CreditCardData card = new CreditCardData();
        card.setNumber("6900460430001234566");
        card.setExpMonth(12);
        card.setExpYear(2024);
        card.setCvn("101");
//        card.setValue("6900460430001234566=24121014656100000");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        BuilderException builderException = assertThrows(BuilderException.class,
                () -> card.charge(new BigDecimal(30))
                        .withCurrency("USD")
                        .withProductData(productData)
                        .withFleetData(null)
                        .execute());
        assertEquals("The purchase device sequence number cannot be null for WEX transactions.", builderException.getMessage());
    }

    @Test
    public void test_Wex_sale_Address_code_coverage() throws ApiException {

        address = new Address();
        address.setName("My STORE            ");
        address.setStreetAddress1("1 MY STREET       ");
        address.setCity("JEFFERSONVILLE  ");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        acceptorConfig.setAddress(address);
        config.setAcceptorConfig(acceptorConfig);

        CreditTrackData card = new CreditTrackData();
        card.setValue(";6900460430001234566=22124012203100001?");

        FleetData fleetData = new FleetData();
        fleetData.setServicePrompt("00");
        fleetData.setDriverId("456320");
        fleetData.setOdometerReading("100");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


}