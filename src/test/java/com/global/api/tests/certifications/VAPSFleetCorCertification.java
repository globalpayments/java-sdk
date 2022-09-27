package com.global.api.tests.certifications;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.FleetData;
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
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class VAPSFleetCorCertification {
    private CreditCardData card;
    private CreditTrackData track;
    private ProductData productData;
    private FleetData fleetData;

    public VAPSFleetCorCertification() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");
//        acceptorConfig.setOperatingEnvironment(OperatingEnvironment.OnPremises_CardAcceptor_Unattended); //ICR


        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0001126198408");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541"); //Inside
//        config.setMerchantType("5542"); //outside

        ServicesContainer.configureService(config);


//        ServicesContainer.configureService(config, "ICR");

        //Company- 044
        //Unit Number- 00011261984
        //Terminal ID- 08

        fleetData = new FleetData();

        //        FuelMan Fleet
//        card = TestCards.FuelmanFleetManual(true, true);
        track = TestCards.FuelmanFleet();

//         FleetWide
//        card = TestCards.FleetWideManual(true, true);
//        track = TestCards.FleetWide();
    }

    @Test // working
    public void test_001_FuelMan_swipe_preAuthorizaiton() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(15))
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
    }

    @Test // working
    public void test_001_Fleetwide_swipe_preAuthorizaiton() throws ApiException {
        track = TestCards.FleetWide();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(15))
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
    }


    //PreAuthorization
    @Test
    public void test_001_FuelMan_swipe_preAuthorizaiton_Completion() throws ApiException {
//        track = TestCards.FleetWide();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(15))
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

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(13), new BigDecimal(13));
//        productData.add("021", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));
//        productData.add("062", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));
//        productData.add("102", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));

        Transaction response = preRresponse.preAuthCompletion(new BigDecimal(13))
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
    public void test_002_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("222");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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
    public void test_002_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("222");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

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
    public void test_003_FuelMan_swipe_Void() throws ApiException {

        fleetData.setOdometerReading("222");
        fleetData.setDriverId("11411");

            ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
            productData.add("002", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

            Transaction sale = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withProductData(productData)
                    .withFleetData(fleetData)
                    .execute();
            assertNotNull(sale.getReceiptText());
            assertNotNull(sale);
            assertEquals("000", sale.getResponseCode());

            Transaction response = sale.voidTransaction(new BigDecimal(10))
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
    public void test_003_FleetWide_swipe_Void() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("222");
        fleetData.setDriverId("11411");

            ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
            productData.add("002", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

            Transaction sale = track.charge(new BigDecimal(10))
                    .withCurrency("USD")
                    .withProductData(productData)
                    .withFleetData(fleetData)
                    .execute();
            assertNotNull(sale.getReceiptText());
            assertNotNull(sale);
            assertEquals("000", sale.getResponseCode());

            Transaction response = sale.voidTransaction(new BigDecimal(10))
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
    public void test_004_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("333");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("020", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(9), new BigDecimal(9));

        Transaction response = track.charge(new BigDecimal(9))
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
    public void test_004_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("333");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("020", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(9), new BigDecimal(9));

        Transaction response = track.charge(new BigDecimal(9))
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
    public void test_005_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("444");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("019", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(8), new BigDecimal(8));

        Transaction response = track.charge(new BigDecimal(8))
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
    public void test_005_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("444");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("019", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(8), new BigDecimal(8));

        Transaction response = track.charge(new BigDecimal(8))
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

    //PreAuthorization - Outside Transaction
    @Test
    public void test_021_FuelMan_swipe_preAuth() throws ApiException {
//        track = TestCards.FleetWide();

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(5))
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
    }

    //PreAuthorization completion - outside Transaction
    @Test
    public void test_021_FuelMan_swipe_AuthCapture() throws ApiException {
//        track = TestCards.FleetWide();
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
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

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
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
    public void test_021_FuelMan_swipe_AuthCapture_cards() throws ApiException {
        card = TestCards.VoyagerManual(true, true);
        track = TestCards.VoyagerSwipe();

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
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
        productData.add("01", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));
        productData.add("45", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));

        Transaction response = preRresponse.capture(new BigDecimal(30))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
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

    //PreAuthorization Multiple products - Outside Transaction
    @Test
    public void test_022_FuelMan_swipe_preAuth() throws ApiException {
        //        track = TestCards.FleetWide();

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(5))
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
    }

    //PreAuthorization Multiple products - Outside Transaction
    @Test
    public void test_022_FuelMan_swipe_AuthCapture() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(5))
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

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("021", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));
        productData.add("062", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));
        productData.add("102", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));

        Transaction response = preRresponse.capture(new BigDecimal(5))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute("ICR");
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

    //Prepay
    @Test
    public void test_006_FleetWide_swipe_AuthCapture() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        Transaction preRresponse = track.authorize(new BigDecimal(30))
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

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(30), new BigDecimal(30));

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
        assertEquals("201", pmi.getFunctionCode());

        // check response
        assertEquals("000", response.getResponseCode());
    }

    //Motor oil
    @Test
    public void test_007_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("819");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));

        Transaction response = track.charge(new BigDecimal(5))
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
    public void test_007_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("819");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));

        Transaction response = track.charge(new BigDecimal(5))
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

    //car wash
    @Test
    public void test_008_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("200");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.OtherOrUnknown,new BigDecimal(1), new BigDecimal(4), new BigDecimal(4));

        Transaction response = track.charge(new BigDecimal(4))
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
    public void test_008_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("200");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("102", UnitOfMeasure.OtherOrUnknown,new BigDecimal(1), new BigDecimal(4), new BigDecimal(4));

        Transaction response = track.charge(new BigDecimal(4))
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

    //Cold Dispensed Beverages - 432
    @Test
    public void test_009_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("400");
        fleetData.setDriverId("123");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("432", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));

        Transaction response = track.charge(new BigDecimal(2))
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
    public void test_009_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("400");
        fleetData.setDriverId("123");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("432", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));

        Transaction response = track.charge(new BigDecimal(2))
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

    //Regulare unleaded and Mid grade
    @Test
    public void test_010_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("111222");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("002", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = track.charge(new BigDecimal(30))
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
    public void test_010_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("111222");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("002", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));

        Transaction response = track.charge(new BigDecimal(30))
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

    //Discount
    @Test
    public void test_011_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("222461");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("900", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));

        Transaction response = track.charge(new BigDecimal(15))
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
    public void test_011_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("222461");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        productData.add("900", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));

        Transaction response = track.charge(new BigDecimal(15))
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

    //Discount & Tax
    @Test
    public void test_012_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("400461");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("101", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("900", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));
        productData.add("950", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));


        Transaction response = track.charge(new BigDecimal(25))
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
    public void test_012_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();
        fleetData.setOdometerReading("400461");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("101", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("900", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(5), new BigDecimal(5));
        productData.add("950", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(2), new BigDecimal(2));


        Transaction response = track.charge(new BigDecimal(25))
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
    public void test_013_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("819400");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.MiniServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("432", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("102", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("950", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(3), new BigDecimal(3));


        Transaction response = track.charge(new BigDecimal(3))
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
    public void test_013_Fleetwide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("819400");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.MiniServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("101", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("432", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("102", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(1), new BigDecimal(1));
        productData.add("950", UnitOfMeasure.OtherOrUnknown, new BigDecimal(1), new BigDecimal(3), new BigDecimal(3));


        Transaction response = track.charge(new BigDecimal(3))
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
    public void test_014_FuelMan_swipe_sale() throws ApiException {

        fleetData.setOdometerReading("445151");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("021", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("062", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));

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
    public void test_014_FleetWide_swipe_sale() throws ApiException {
        track = TestCards.FleetWide();

        fleetData.setOdometerReading("445151");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific);
        productData.add("021", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));
        productData.add("062", UnitOfMeasure.Liters, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));

        Transaction response = track.charge(new BigDecimal(30))
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
    public void test_015_FuelMan_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70764912345100140");
        card.setExpMonth(12);
        card.setExpYear(2049);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_015_FleetWide_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70768512345200115");
        card.setExpMonth(12);
        card.setExpYear(2099);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_016_FuelMan_Manual_sale() throws ApiException {

        card = TestCards.FuelmanFleetManual(true,true);
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_016_FleetWide_Manual_sale() throws ApiException {

        card = TestCards.FleetWideManual(true, true);

        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_017_FuelMan_Manual_sale() throws ApiException {
     card = TestCards.FuelmanFleetManual(true,true);

        fleetData.setOdometerReading(" ");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_018_FuelMan_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70764912345100040");
        card.setExpMonth(12);
        card.setExpYear(2099);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);


        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_018_FleetWide_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(12);
        card.setExpYear(2099);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);


        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_019_FleetWide_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(12);
        card.setExpYear(2049);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);


        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_020_FuelMan_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70764912345100040");
        card.setExpMonth(00);
        card.setExpYear(2007);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);


        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
    public void test_020_FleetWide_Manual_sale() throws ApiException {
        card = new CreditCardData();
        card.setNumber("70768512345200005");
        card.setExpMonth(00);
        card.setExpYear(2007);
        card.setCvn("123");
        card.setCardPresent(true);
        card.setReaderPresent(true);


        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(10), new BigDecimal(10));

        Transaction response = card.charge(new BigDecimal(10))
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
//    @Ignore("Can not Test Partial Approval")
    public void test_credit_partial_approval() throws ApiException {
        fleetData.setOdometerReading("111");
        fleetData.setDriverId("11411");
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("014", UnitOfMeasure.Gallons, new BigDecimal(1), new BigDecimal(20), new BigDecimal(20));
        Transaction response = track.charge(new BigDecimal("20"))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .execute();
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

//        BigDecimal authorizedAmount = response.getAuthorizedAmount();
//        assertNotEquals(new BigDecimal("30"), authorizedAmount);

    }

}

