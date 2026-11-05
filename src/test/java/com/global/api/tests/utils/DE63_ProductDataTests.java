package com.global.api.tests.utils;

import com.global.api.network.NetworkMessage;
import com.global.api.network.elements.DE63_ProductData;
import com.global.api.network.elements.DE63_ProductDataEntry;
import com.global.api.network.entities.ProductData;
import com.global.api.network.enums.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Offline regression tests for DE 63 (Product Data) parse/serialize round trip.
 * <p>
 * The transaction token / implied data-collect re-serializes DE 63 via
 * {@code fromByteArray -> toByteArray} (VapsConnector.buildImpliedCapture). These tests guarantee the
 * re-serialized DE 63 is byte-for-byte identical to the original, including duplicate product codes
 * produced by fleet rollups (Voyager 33, Mastercard 99, Visa 90, FleetCor/WEX 400).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DE63_ProductDataTests {

    private static final String VOYAGER_ISSUE_26_DE63 =
            "02O00633\\U07\\2400\\2700\\23\\U06\\2200\\1200\\14\\U04\\2200\\800\\43\\U03\\2200\\600\\34\\U01\\2596\\596\\33\\U02\\\\800\\";

    // region helpers

    /** Parses the value, asserts the re-serialized value is identical and the product count matches the header. */
    private static DE63_ProductData assertRoundTrip(String original) {
        DE63_ProductData parsed = new DE63_ProductData().fromByteArray(original.getBytes());
        assertEquals("re-serialized DE 63 must match the original", original, parsed.toString());
        assertEquals("product count must match the DE 63 header", standardHeaderCount(original), parsed.getProductCount());
        assertEquals(parsed.getProductCount(), parsed.getOrderedProductEntries().size());
        return parsed;
    }

    /** Product count (3 digits) of a GlobalPaymentsStandardFormat DE 63 value. */
    private static int standardHeaderCount(String de63) {
        return Integer.parseInt(de63.substring(3, 6));
    }

    private static long countCode(DE63_ProductData productData, String code) {
        return productData.getOrderedProductEntries().stream().filter(e -> code.equals(e.getCode())).count();
    }

    private static DE63_ProductDataEntry lastEntry(DE63_ProductData productData) {
        List<DE63_ProductDataEntry> entries = productData.getOrderedProductEntries();
        return entries.get(entries.size() - 1);
    }

    /** Serializes product data exactly as the connector does for the given fleet card type. */
    private static String serialize(ProductData productData, String cardType) {
        DE63_ProductData de63 = productData.toDataElement();
        de63.setCardType(cardType);
        return de63.toString();
    }

    private static ProductData standardProductData() {
        return new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.IssuerSpecific, ProductDataFormat.GlobalPaymentsStandardFormat);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    // endregion

    // U1 - Voyager rollup "33" colliding with a real product "33" (Issue 26)
    @Test
    public void test_01_voyager_rollup33_with_real_product33_round_trip() {
        ProductData productData = standardProductData();
        productData.addNonFuel("33", UnitOfMeasure.Units, bd("7"), bd("4.00"), bd("27.00"));
        productData.addNonFuel("23", UnitOfMeasure.Units, bd("6"), bd("2.00"), bd("12.00"));
        productData.addNonFuel("14", UnitOfMeasure.Units, bd("4"), bd("2.00"), bd("8.00"));
        productData.addNonFuel("43", UnitOfMeasure.Units, bd("3"), bd("2.00"), bd("6.00"));
        productData.addNonFuel("34", UnitOfMeasure.Units, bd("1"), bd("5.96"), bd("5.96"));
        productData.addNonFuel("40", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));
        productData.addNonFuel("41", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));

        String original = serialize(productData, "VoyagerFleet");
        assertEquals(VOYAGER_ISSUE_26_DE63, original);

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(6, parsed.getProductCount());
        assertEquals(2, countCode(parsed, "33"));

        DE63_ProductDataEntry rollup = lastEntry(parsed);
        assertEquals("33", rollup.getCode());
        assertNull(rollup.getPrice());
        assertEquals(0, bd("8.00").compareTo(rollup.getAmount()));
    }

    // U2 - Mastercard Fleet rollup "99" colliding with a real product "99"
    @Test
    public void test_02_mastercard_rollup99_with_real_product99_round_trip() {
        ProductData productData = standardProductData();
        productData.addNonFuel("99", UnitOfMeasure.Units, bd("1"), bd("50.00"), bd("50.00"));
        productData.addNonFuel("10", UnitOfMeasure.Units, bd("1"), bd("40.00"), bd("40.00"));
        productData.addNonFuel("11", UnitOfMeasure.Units, bd("1"), bd("30.00"), bd("30.00"));
        productData.addNonFuel("12", UnitOfMeasure.Units, bd("1"), bd("20.00"), bd("20.00"));
        productData.addNonFuel("13", UnitOfMeasure.Units, bd("1"), bd("15.00"), bd("15.00"));
        productData.addNonFuel("14", UnitOfMeasure.Units, bd("1"), bd("5.00"), bd("5.00"));
        productData.addNonFuel("15", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));

        String original = serialize(productData, "MCFleet");
        System.out.println("Original DE 63: " + original);
        assertEquals(6, standardHeaderCount(original));

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(2, countCode(parsed, "99"));

        DE63_ProductDataEntry rollup = lastEntry(parsed);
        assertEquals("99", rollup.getCode());
        assertNull(rollup.getPrice());
        assertEquals(0, bd("9.00").compareTo(rollup.getAmount()));
    }

    // U3 - Visa Fleet rollup "90" colliding with a real product "90"
    @Test
    public void test_03_visafleet_rollup90_with_real_product90_round_trip() {
        ProductData productData = standardProductData();
        productData.addNonFuel("90", UnitOfMeasure.Units, bd("1"), bd("50.00"), bd("50.00"));
        productData.addNonFuel("10", UnitOfMeasure.Units, bd("1"), bd("40.00"), bd("40.00"));
        productData.addNonFuel("11", UnitOfMeasure.Units, bd("1"), bd("30.00"), bd("30.00"));
        productData.addNonFuel("12", UnitOfMeasure.Units, bd("1"), bd("20.00"), bd("20.00"));
        productData.addNonFuel("13", UnitOfMeasure.Units, bd("1"), bd("15.00"), bd("15.00"));
        productData.addNonFuel("14", UnitOfMeasure.Units, bd("1"), bd("5.00"), bd("5.00"));
        productData.addNonFuel("15", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));

        String original = serialize(productData, "VisaFleet");
        assertEquals(6, standardHeaderCount(original));

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(2, countCode(parsed, "90"));
        assertEquals("90", lastEntry(parsed).getCode());
        assertNull(lastEntry(parsed).getPrice());
    }

    // U4 - FleetCor (Fuelman / FleetWide) rollup "400", max 4 products, colliding with a real product "400"
    @Test
    public void test_04_fleetcor_rollup400_with_real_product400_round_trip() {
        for (String cardType : new String[] {"FuelmanFleet", "FleetWide"}) {
            ProductData productData = new ProductData(ServiceLevel.Other_NonFuel, ProductCodeSet.Conexxus_3_Digit, ProductDataFormat.GlobalPaymentsStandardFormat);
            productData.addNonFuel("400", UnitOfMeasure.Units, bd("1"), bd("50.00"), bd("50.00"));
            productData.addNonFuel("102", UnitOfMeasure.Units, bd("1"), bd("40.00"), bd("40.00"));
            productData.addNonFuel("103", UnitOfMeasure.Units, bd("1"), bd("30.00"), bd("30.00"));
            productData.addNonFuel("104", UnitOfMeasure.Units, bd("1"), bd("20.00"), bd("20.00"));
            productData.addNonFuel("105", UnitOfMeasure.Units, bd("1"), bd("10.00"), bd("10.00"));

            String original = serialize(productData, cardType);
            assertEquals(cardType, 4, standardHeaderCount(original));

            DE63_ProductData parsed = assertRoundTrip(original);
            assertEquals(cardType, 2, countCode(parsed, "400"));
            assertEquals(cardType, "400", lastEntry(parsed).getCode());
            assertEquals(cardType, 0, bd("30.00").compareTo(lastEntry(parsed).getAmount()));
        }
    }

    // U5 - WEX: 2 fuel + more than 6 non-fuel, rollup "400" (unit O), colliding with a real product "400"
    @Test
    public void test_05_wex_two_fuel_and_rollup400_round_trip() {
        ProductData productData = standardProductData();
        productData.addFuel("001", UnitOfMeasure.Gallons, bd("10"), bd("3.459"), bd("34.59"));
        productData.addFuel("002", UnitOfMeasure.Gallons, bd("5"), bd("4.000"), bd("20.00"));
        productData.addNonFuel("400", UnitOfMeasure.Units, bd("1"), bd("50.00"), bd("50.00"));
        productData.addNonFuel("103", UnitOfMeasure.Units, bd("1"), bd("40.00"), bd("40.00"));
        productData.addNonFuel("104", UnitOfMeasure.Units, bd("1"), bd("30.00"), bd("30.00"));
        productData.addNonFuel("105", UnitOfMeasure.Units, bd("1"), bd("20.00"), bd("20.00"));
        productData.addNonFuel("106", UnitOfMeasure.Units, bd("1"), bd("15.00"), bd("15.00"));
        productData.addNonFuel("107", UnitOfMeasure.Units, bd("1"), bd("5.00"), bd("5.00"));
        productData.addNonFuel("108", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));

        String original = serialize(productData, "WexFleet");
        assertEquals(8, standardHeaderCount(original));

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(2, countCode(parsed, "400"));

        DE63_ProductDataEntry rollup = lastEntry(parsed);
        assertEquals("400", rollup.getCode());
        assertEquals(UnitOfMeasure.OtherOrUnknown, rollup.getUnitOfMeasure());
        assertNull(rollup.getPrice());
        assertEquals(0, bd("9.00").compareTo(rollup.getAmount()));
    }

    // U6 - Voyager with fuel + discount codes 35/36 placed before the rollup
    @Test
    public void test_06_voyager_rollup_with_discounts_round_trip() {
        ProductData productData = standardProductData();
        productData.addFuel("04", UnitOfMeasure.Gallons, bd("1"), bd("10"), bd("10"));
        productData.addNonFuel(ProductCode.Tires, UnitOfMeasure.OtherOrUnknown, bd("2"), bd("12"), bd("24"));
        productData.addNonFuel(ProductCode.Oil_Change, UnitOfMeasure.OtherOrUnknown, bd("2"), bd("14"), bd("28"));
        productData.addNonFuel(ProductCode.Batteries, UnitOfMeasure.OtherOrUnknown, bd("2"), bd("13"), bd("26"));
        productData.addNonFuel(ProductCode.Wipers, UnitOfMeasure.OtherOrUnknown, bd("2"), bd("15"), bd("30"));
        productData.addNonFuel(ProductCode.Brake_Service, UnitOfMeasure.OtherOrUnknown, bd("2"), bd("11"), bd("22"));
        productData.addNonFuel("35", UnitOfMeasure.OtherOrUnknown, bd("1"), bd("23"), bd("23"));
        productData.addNonFuel("36", UnitOfMeasure.OtherOrUnknown, bd("1"), bd("25"), bd("25"));

        String original = serialize(productData, "VoyagerFleet");
        assertEquals(6, standardHeaderCount(original));

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(1, countCode(parsed, "35"));
        assertEquals(1, countCode(parsed, "36"));
        assertEquals("33", lastEntry(parsed).getCode());
    }

    // U7 - no duplicate codes: behaviour must be unchanged
    @Test
    public void test_07_no_duplicates_round_trip() {
        assertRoundTrip("02O00233\\U07\\2400\\2700\\23\\U06\\2200\\1200\\");
        assertRoundTrip("00O00182\\\\\\75\\");
        assertRoundTrip("02O000");
    }

    // U8 - empty quantity / price fields are preserved
    @Test
    public void test_08_empty_fields_round_trip() {
        DE63_ProductData parsed = assertRoundTrip("02O00145\\\\\\2500\\");
        DE63_ProductDataEntry entry = parsed.getOrderedProductEntries().get(0);
        assertNull(entry.getUnitOfMeasure());
        assertNull(entry.getQuantity());
        assertNull(entry.getPrice());
        assertEquals(0, bd("25.00").compareTo(entry.getAmount()));
    }

    // U9 - getOrderedProductEntries() returns a defensive copy
    @Test
    public void test_09_ordered_entries_is_defensive_copy() {
        DE63_ProductData parsed = assertRoundTrip(VOYAGER_ISSUE_26_DE63);

        List<DE63_ProductDataEntry> entries = parsed.getOrderedProductEntries();
        entries.clear();

        assertEquals(6, parsed.getProductCount());
        assertEquals(6, parsed.getOrderedProductEntries().size());
        assertEquals(VOYAGER_ISSUE_26_DE63, parsed.toString());
    }

    // U10 - keyed map keeps the first occurrence per code (first-wins)
    @Test
    public void test_10_map_keeps_first_occurrence() {
        DE63_ProductData parsed = assertRoundTrip(VOYAGER_ISSUE_26_DE63);

        assertEquals(5, parsed.getProductDataEntries().size());

        DE63_ProductDataEntry first = parsed.getProductDataEntries().get("33");
        assertNotNull(first);
        assertEquals(UnitOfMeasure.Units, first.getUnitOfMeasure());
        assertEquals(0, bd("7").compareTo(first.getQuantity()));
        assertEquals(0, bd("4.00").compareTo(first.getPrice()));
        assertEquals(0, bd("27.00").compareTo(first.getAmount()));
        assertEquals(first.getAmount(), parsed.getOrderedProductEntries().get(0).getAmount());
    }

    // U11 - ANSI X9 format is unaffected (still map based)
    @Test
    public void test_11_ansi_x9_unaffected() {
        String original = "13S05001G2417\\31199\\500\\411U010\\2179\\1790\\461O\\\\99\\905O\\\\179\\950O\\\\97\\";

        DE63_ProductData parsed = new DE63_ProductData().fromByteArray(original.getBytes());
        assertEquals(ProductDataFormat.ANSI_X9_TG23_Format, parsed.getProductDataFormat());
        assertEquals(5, parsed.getProductCount());
        assertEquals(5, parsed.getProductDataEntries().size());
        assertEquals(original, parsed.toString());
    }

    // U13 - same path as VapsConnector.buildImpliedCapture: NetworkMessage -> getDataElement(DE_063) -> set(DE_063)
    @Test
    public void test_13_network_message_implied_capture_path() {
        NetworkMessage request = new NetworkMessage();
        request.set(DataElementId.DE_063, VOYAGER_ISSUE_26_DE63);
        byte[] buffer = request.buildMessage();

        NetworkMessage parsedRequest = NetworkMessage.parse(buffer, Iso8583MessageType.CompleteMessage);
        DE63_ProductData productData = parsedRequest.getDataElement(DataElementId.DE_063, DE63_ProductData.class);
        assertNotNull(productData);
        assertEquals(6, productData.getProductCount());

        NetworkMessage impliedCapture = new NetworkMessage();
        impliedCapture.set(DataElementId.DE_063, productData);
        assertEquals(VOYAGER_ISSUE_26_DE63, impliedCapture.getString(DataElementId.DE_063));
    }

    // U14 - builder path without card type: same code in fuel and non-fuel maps -> header count matches entries
    @Test
    public void test_14_same_code_fuel_and_nonfuel_count_matches_entries() {
        ProductData productData = new ProductData(ServiceLevel.FullServe, ProductCodeSet.IssuerSpecific, ProductDataFormat.GlobalPaymentsStandardFormat);
        productData.addFuel("01", UnitOfMeasure.Gallons, bd("2"), bd("3.00"), bd("6.00"));
        productData.addNonFuel("01", UnitOfMeasure.Units, bd("1"), bd("5.00"), bd("5.00"));
        productData.addNonFuel("02", UnitOfMeasure.Units, bd("1"), bd("4.00"), bd("4.00"));

        String original = productData.toDataElement().toString();
        assertEquals(3, standardHeaderCount(original));

        DE63_ProductData parsed = assertRoundTrip(original);
        assertEquals(2, countCode(parsed, "01"));
    }
}

