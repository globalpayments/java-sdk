package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE48_34_MessageConfiguration;
import com.global.api.network.elements.DE48_MessageControl;
import com.global.api.network.entities.FleetData;
import com.global.api.network.entities.ProductData;
import com.global.api.network.entities.TransactionMatchingData;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VaspWexEmvTests {


    public VaspWexEmvTests() throws ApiException {
        AcceptorConfig acceptorConfig = new AcceptorConfig();

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
         acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportedEncryptionType(EncryptionType.TEP2);
        acceptorConfig.setSupportsEmvPin(true);
        //DE48-34 message configuration values
        acceptorConfig.setPerformDateCheck(true);
        acceptorConfig.setEchoSettlementData(true);
        acceptorConfig.setIncludeLoyaltyData(false);
        acceptorConfig.setSupportWexAdditionalProducts(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setUniqueDeviceId("2001");
        config.setCompanyId("0044");
        config.setTerminalId("0001126198308");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());
        config.setMerchantType("5541");
        ServicesContainer.configureService(config);
    }


    /**
     * Sales test case
     * @throws ApiException
     */
    @Test
    //  @Ignore
    public void test_wex_emv_sales_4804_V412_ON_I_WX4_1200_1() throws ApiException {

        CreditTrackData card = new CreditTrackData();
//        card.setValue("6900460420006149231=27121004804220000");
        card.setValue("6900460420006149231=19021013019200009");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("9876");
        fleetData.setDriverId("1234");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4847_V412_ON_I_WX4_1200_2() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004847120001");

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("123450");
        fleetData.setOdometerReading("123450");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4892_V412_ON_I_WX4_1200_3() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27124004892120000");

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("12345");
        fleetData.setOdometerReading("42586");
        fleetData.setDepartment("171");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4918_V412_ON_I_WX4_1200_4() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004918120000");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("1234");
        fleetData.setDriverId("1234");
        fleetData.setEnteredData("1234"); //F-  new change

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4922_V412_ON_I_WX4_1200_5() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004922120001");

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setTripNumber("123456");
        fleetData.setUnitNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4924_V412_ON_I_WX4_1200_6() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004924120000");

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setEnteredData("123456"); //new change
        fleetData.setTripNumber("123456");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }
    @Test
    //  @Ignore
    public void test_wex_emv_4926_V412_ON_I_WX4_1200_7() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004926120001");

        FleetData fleetData = new FleetData();
        fleetData.setVehicleNumber("123456");
        fleetData.setOdometerReading("123456");
        fleetData.setTrailerReferHours("123456"); //C
        fleetData.setUnitNumber("123456");//A

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));


        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }


    @Test
//    @Ignore
    public void test_4844_V412_ON_I_WX4_1220_1() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27120014844120000");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Heartland);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
//    @Ignore
    public void test_4846_V412_ON_I_WX4_1220_2() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27120014844120000");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("123450");
        fleetData.setDriverId("123450");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
//    @Ignore
    public void test_4848_V412_ON_I_WX4_1220_3() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27120014844120000");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("123450");
        fleetData.setDriverId("123450");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute("outside");
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
//    @Ignore
    public void test_4873_V412_ON_I_WX4_1220_4() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27120014844120000");

        FleetData fleetData = new FleetData();
        fleetData.setOdometerReading("23235");
        fleetData.setDriverId("3887");
        fleetData.setJobNumber("50");

        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.SelfServe, ProductCodeSet.Conexxus_3_Digit);
        productData.add("100", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute("outside");
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }

    @Test
    public void test_wex_preAuth_WAP() throws ApiException {

        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27120014844120000");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Heartland);
        productData.add("01", UnitOfMeasure.Units, new BigDecimal("0001"), new BigDecimal(21000), new BigDecimal(10));

        Transaction response = card.authorize(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());

        productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit);
        productData.add("400", UnitOfMeasure.Units, new BigDecimal(0001), new BigDecimal(21000), new BigDecimal(10));

        Transaction captureResponse = response.capture(new BigDecimal(45))
                .withCurrency("USD")
                .withProductData(productData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(captureResponse);
        // check response
        assertEquals("400", captureResponse.getResponseCode());
    }
    @Test
    public void test_wex_emv_sale_10327() throws ApiException {
        CreditTrackData card = new CreditTrackData();
        card.setValue("6900460420006149231=27121004847120001");

        FleetData fleetData = new FleetData();
        fleetData.setDriverId("1234");
        fleetData.setOdometerReading("9876");

        ProductData productData = new ProductData(ServiceLevel.Other_NonFuel,ProductCodeSet.Conexxus_3_Digit);
        productData.add("001", UnitOfMeasure.Gallons, new BigDecimal("10.720"), new BigDecimal(34664), new BigDecimal(50));

        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .withProductData(productData)
                .withFleetData(fleetData)
                .withTagData("4F07A0000007681010820239008407A00000076810108A025A33950500800080009A032021039B02E8009C01005F24032212315F280208405F2A0208405F3401029F02060000000001009F03060000000000009F0607A00000076810109F07023D009F080201539F090200019F0D05BC308088009F1A0208409F0E0500400000009F0F05BCB08098009F10200FA502A830B9000000000000000000000F0102000000000000000000000000009F2103E800259F2608DD53340458AD69B59F2701809F34031E03009F3501169F3303E0F8C89F360200019F37045876B0989F3901009F4005F000F0A0019F410400000000")
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "000", response.getResponseCode());
    }

}
