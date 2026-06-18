package com.global.api.tests.network.vaps;


import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import com.global.api.tests.testdata.TestCards;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsFleetCorTests {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;

    public VapsFleetCorTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);


        // hardware software config values
        acceptorConfig.setHardwareLevel("S1");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportWexAvailableProducts(true);
        acceptorConfig.setSupportsEmvPin(true);
        acceptorConfig.setVisaFleet2(false);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0009");
        config.setTerminalId("0001126198301");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");


        fleetData = new FleetData();
//        fleetData.setServicePrompt("00");
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");


        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.GlobalPayments);
        productData.add("001", UnitOfMeasure.Gallons, 1, 10);

//        FuelMan Fleet
        card = TestCards.FuelmanFleetManual(true, true);
        track = TestCards.FuelmanFleet();

//         FleetWide
//        card = TestCards.FleetWideManual(true, true);
//        track = TestCards.FleetWide();

    }

    //Manual Authorization
    @Test
    public void test_001_manual_authorization() throws ApiException {
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

    @Test
    public void test_001_swipe_authorization() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(10))
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

    //Manual Sale
    @Test
    public void test_002_manual_sale() throws ApiException {
        productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(10), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

//        // check response
        assertEquals("000", response.getResponseCode());
    }

    //SALE
    @Test
    public void test_swipe_sale() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(50))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Void
    @Test
    public void test_006_swipe_void() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.GlobalPayments);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction sale = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(sale.getReceiptText());
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        Transaction response = sale.voidTransaction()
                .withReferenceNumber(sale.getReceiptText())
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


    @Test
    @Ignore("Can not Test Partial Approval")
    public void test_credit_partial_approval() throws ApiException {
        fleetData.setOdometerReading("111");
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        Transaction response = track.charge(new BigDecimal("30"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("30"), authorizedAmount);

    }

    @Test
    @Ignore("Can not Test Partial Approval")
    public void test_000_credit_Void_partial_approval() throws ApiException {
        fleetData.setOdometerReading("111");
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        Transaction response = track.charge(new BigDecimal("30"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("30"), authorizedAmount);

        Transaction voidResponse = response.voidTransaction(authorizedAmount)
                .withCurrency("USD")
                .execute();
        assertNotNull(voidResponse);

        PriorMessageInformation pmi = voidResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("000", voidResponse.getResponseCode());
    }


    //auth capture
    @Test
    public void test_016_ICR_authCapture() throws ApiException {
        Transaction response = track.authorize(new BigDecimal(1), true)
                .withCurrency("USD")
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("101", pmi.getFunctionCode());

        // check response
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        Transaction captureResponse = response.capture(new BigDecimal(12))
                .withCurrency("USD")
                .withTerminalError(true)
                .execute();
        assertNotNull(captureResponse);

        // check message data
        pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("1376", pmi.getMessageReasonCode());
        assertEquals("202", pmi.getFunctionCode());

        // check response
        assertEquals("000", captureResponse.getResponseCode());
    }

    //Reversal
    @Test
    public void test_020_reversal() throws ApiException {
        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
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
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("400", pmi.getFunctionCode());
        assertEquals("4351", pmi.getMessageReasonCode());

        // check response
        assertEquals("400", reversal.getResponseCode());
    }

    @Test
    public void test_014_swipe_sale_product_14() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }


    @Test
    public void test_021_swipe_sale_mc_product_50() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("050", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_026_swipe_sale_product_all() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("059", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("009", UnitOfMeasure.Quarts, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("027", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("023", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("014", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("033", UnitOfMeasure.Units, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(70))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //  AddFuel() & AddNonFuel()
    @Test
    public void test_001_sale_Fuel_nonFuel() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Liters, new BigDecimal("5.12"), new BigDecimal("50.00"), new BigDecimal("111.2"));
        productData.addNonFuel(ProductCode.Filters, UnitOfMeasure.Units, new BigDecimal("5.12"), new BigDecimal("50.00"), new BigDecimal("111.2"));
        productData.addNonFuel(ProductCode.Natural_Gas, UnitOfMeasure.Quarts, new BigDecimal("5.12"), new BigDecimal("50.00"), new BigDecimal("111.2"));

        Transaction response = card.charge(new BigDecimal("111.2"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_002_sale_Fuel_nonFuel_track() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.GlobalPayments, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel(ProductCode.UNLEADED_PLUS_ETHANOL, UnitOfMeasure.Liters, new BigDecimal("5.12"), new BigDecimal("50.00"), new BigDecimal("111.2"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.Units, new BigDecimal("5.12"), new BigDecimal("50.00"), new BigDecimal("111.2"));

        Transaction response = track.charge(new BigDecimal("111.2"))
                .withCurrency("USD")
                .withFleetData(fleetData)
                .withProductData(productData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    @Test
    public void test_FleetCorSale_Exactly_Four_Products_No_Combining() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("10.00"), new BigDecimal("20.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");

        Transaction response = track.charge(new BigDecimal("80.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: only non-fuel products, more than 4 should combine into product code 400
    @Test
    public void test_FleetCorSale_NonFuel_Only_Combining() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("25.00"), new BigDecimal("25.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("12.00"), new BigDecimal("12.00"));
        productData.addNonFuel("069", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("3.00"), new BigDecimal("5.00"), new BigDecimal("15.00"));

        Transaction response = track.charge(new BigDecimal("92.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: fuel first, up to 4 products, non-fuel in decreasing price order
    @Test
    public void test_FleetCorSale_Fuel_First_With_NonFuel_Decreasing_Order() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("061", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("16.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("15.00"), new BigDecimal("15.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        Transaction response = track.charge(new BigDecimal("71.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: more than 4 non-fuel products should be combined using product code 400
    @Test
    public void test_FleetCorSale_NonFuel_Combining_More_Than_Four_Products() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel("069", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("2.00"), new BigDecimal("5.00"), new BigDecimal("10.00"));

        Transaction response = track.charge(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: only fuel, single product
    @Test
    public void test_FleetCorSale_Fuel_Only_Single_Product() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("061", UnitOfMeasure.Gallons, new BigDecimal("15.00"), new BigDecimal("3.50"), new BigDecimal("52.50"));

        Transaction response = track.charge(new BigDecimal("52.50"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor Authorize: exactly 4 products (fuel + 3 non-fuel) - no combining needed
    @Test
    public void test_FleetCorAuthorize_Exactly_Four_Products_No_Combining() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("10.00"), new BigDecimal("20.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("125630");
        fleetData.setDriverId("11411");

        Transaction response = track.authorize(new BigDecimal("80.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor Authorize: only non-fuel products, more than 4 should combine into product code 400
    @Test
    public void test_FleetCorAuthorize_NonFuel_Only_Combining() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("25.00"), new BigDecimal("25.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("12.00"), new BigDecimal("12.00"));
        productData.addNonFuel("069", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("3.00"), new BigDecimal("5.00"), new BigDecimal("15.00"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction response = track.authorize(new BigDecimal("92.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor Authorize: fuel first, up to 4 products, non-fuel in decreasing price order
    @Test
    public void test_FleetCorAuthorize_Fuel_First_With_NonFuel_Decreasing_Order() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("16.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("15.00"), new BigDecimal("15.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction response = track.authorize(new BigDecimal("71.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor Authorize: more than 4 non-fuel products should be combined using product code 400
    @Test
    public void test_FleetCorAuthorize_NonFuel_Combining_More_Than_Four_Products() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("11.234"), new BigDecimal("20.50"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel("069", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("2.00"), new BigDecimal("5.00"), new BigDecimal("10.00"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction response = track.authorize(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor Authorize: only fuel, single product
    @Test
    public void test_FleetCorAuthorize_Fuel_Only_Single_Product() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("15.00"), new BigDecimal("3.50"), new BigDecimal("52.50"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction response = track.authorize(new BigDecimal("52.50"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    @Test
        public void test_FleetCorAuth_Capture_Fuel_First_With_NonFuel_Decreasing_Order() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("16.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("15.00"), new BigDecimal("15.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction authResponse = track.authorize(new BigDecimal("71.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(authResponse);

        PriorMessageInformation pmi = authResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", authResponse.getResponseCode());

        Transaction response = authResponse.capture()
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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_FleetCorAuth_Capture_Combining_More_Than_Four_Products() throws ApiException {
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("16.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("15.00"), new BigDecimal("15.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));
        productData.addNonFuel("081", UnitOfMeasure.Units, new BigDecimal("2.00"), new BigDecimal("5.00"), new BigDecimal("10.00"));
        productData.addNonFuel("069", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));

        CreditTrackData track = TestCards.FleetWide();
        Transaction authResponse = track.authorize(new BigDecimal("71.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(authResponse);

        PriorMessageInformation pmi = authResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1100", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("100", pmi.getFunctionCode());
        assertEquals("000", authResponse.getResponseCode());

        Transaction response = authResponse.capture()
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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_FleetCorAuth_Capture_Individual() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized),
                track
        );

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("10"));
        productData.addNonFuel("067", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("12"), new BigDecimal("30"));
        productData.addNonFuel("051", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("14"), new BigDecimal("50"));
        productData.addNonFuel("069", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("13"), new BigDecimal("40"));
        productData.addNonFuel("064", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("60"));
        Transaction response = transaction.capture(new BigDecimal("10"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        // check message data
        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1220", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }
    @Test
    public void test_FleetCorAuthorize_Multi_Fuel_Negative() throws UnsupportedOperationException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("15.00"), new BigDecimal("3.50"), new BigDecimal("52.50"));
        productData.addFuel("019", UnitOfMeasure.Pounds, new BigDecimal("12.00"), new BigDecimal("2.250"), new BigDecimal("32.450"));

        assertThrows(UnsupportedOperationException.class, () -> {
                   track.authorize(new BigDecimal("52.50"))
                           .withCurrency("USD")
                           .withProductData(productData)
                           .withFleetData(fleetData)
                           .execute();
               });
    }

    @Test
    public void test_FleetCorSale_Multi_Fuel_Negative() throws UnsupportedOperationException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("15.00"), new BigDecimal("3.50"), new BigDecimal("52.50"));
        productData.addFuel("019", UnitOfMeasure.Pounds, new BigDecimal("12.00"), new BigDecimal("2.250"), new BigDecimal("32.450"));

        assertThrows(UnsupportedOperationException.class, () -> {
            track.charge(new BigDecimal("52.50"))
                    .withCurrency("USD")
                    .withProductData(productData)
                    .withFleetData(fleetData)
                    .execute();
        });
    }
    @Test
    public void test_FleetCorAuthorize_Multi_Fuel_Fuelman_Negative() throws UnsupportedOperationException {
        CreditTrackData track = TestCards.FuelmanFleet();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("15.00"), new BigDecimal("3.50"), new BigDecimal("52.50"));
        productData.addFuel("019", UnitOfMeasure.Pounds, new BigDecimal("12.00"), new BigDecimal("2.250"), new BigDecimal("32.450"));

        assertThrows(UnsupportedOperationException.class, () -> {
            track.authorize(new BigDecimal("52.50"))
                    .withCurrency("USD")
                    .withProductData(productData)
                    .withFleetData(fleetData)
                    .execute();
        });
    }
    @Test
    public void test_FleetCorAuth_Capture_Multi_Fuel_Negative() throws UnsupportedOperationException {
        CreditTrackData track = TestCards.FleetWide();
        Transaction transaction = Transaction.fromNetwork(
                new BigDecimal(10),
                "TYPE04",
                new NtsData(FallbackCode.None, AuthorizerCode.Interchange_Authorized),
                track
        );

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.addFuel("019", UnitOfMeasure.Pounds, new BigDecimal("12.00"), new BigDecimal("2.250"), new BigDecimal("32.450"));
        productData.addFuel("004", UnitOfMeasure.Gallons, new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("10"));
        productData.addNonFuel("067", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("12"), new BigDecimal("30"));
        productData.addNonFuel("051", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("14"), new BigDecimal("50"));
        productData.addNonFuel("069", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("13"), new BigDecimal("40"));
        productData.addNonFuel("064", UnitOfMeasure.OtherOrUnknown, new BigDecimal("1"), new BigDecimal("15"), new BigDecimal("60"));

        assertThrows(UnsupportedOperationException.class,() -> {
            transaction.capture(new BigDecimal("10"))
                    .withCurrency("USD")
                    .withProductData(productData)
                    .withFleetData(fleetData)
                    .execute();
        });
    }

    // FleetCor: 2 discount , 1 fuel & 1 non-fuel products.
    @Test
    public void FleetCorSale_FleetWide_DiscountProducts_AreAppended() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_2.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        Transaction response = track.charge(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: 2 discount 3 non-fuel products should be combined using product code 400
    @Test
    public void FleetCorSale_FleetWide_RollupWhenOverLimit_WithDiscounts() throws ApiException {
        CreditTrackData track = TestCards.FleetWide();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_2.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        Transaction response = track.charge(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: 2 discount , 1 fuel & 1 non-fuel products.
    @Test
    public void FleetCorSale_Fuelman_DiscountProducts_AreAppended() throws ApiException {
        CreditTrackData track = TestCards.FuelmanFleet();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_2.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        Transaction response = track.charge(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }

    // FleetCor: 2 discount 3 non-fuel products should be combined using product code 400
    @Test
    public void FleetCorSale_Fuelman_RollupWhenOverLimit_WithDiscounts() throws ApiException {
        CreditTrackData track = TestCards.FuelmanFleet();
        ProductData productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.addFuel("001", UnitOfMeasure.Gallons, new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"));
        productData.addNonFuel("062", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("20.00"), new BigDecimal("20.00"));
        productData.addNonFuel("050", UnitOfMeasure.Quarts, new BigDecimal("2.00"), new BigDecimal("15.00"), new BigDecimal("30.00"));
        productData.addNonFuel("067", UnitOfMeasure.Units, new BigDecimal("1.00"), new BigDecimal("10.00"), new BigDecimal("10.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_1.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("8.00"), new BigDecimal("8.00"));
        productData.addNonFuel(FleetCorConexxusProductCode.DISCOUNT_2.getValue(), UnitOfMeasure.OtherOrUnknown, new BigDecimal("1.00"), new BigDecimal("5.00"), new BigDecimal("5.00"));

        Transaction response = track.charge(new BigDecimal("113.00"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);

        PriorMessageInformation pmi = response.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("1200", pmi.getMessageTransactionIndicator());
        assertEquals("000900", pmi.getProcessingCode());
        assertEquals("200", pmi.getFunctionCode());
        assertEquals("000", response.getResponseCode());
    }


}

