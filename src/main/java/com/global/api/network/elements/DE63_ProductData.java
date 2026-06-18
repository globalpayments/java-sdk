package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.ReverseStringEnumMap;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.math.RoundingMode;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

public class DE63_ProductData implements IDataElement<DE63_ProductData> {
    private ProductDataFormat productDataFormat = ProductDataFormat.GlobalPaymentsStandardFormat;
    private ProductCodeSet productCodeSet = ProductCodeSet.GlobalPayments;
    private ServiceLevel serviceLevel = ServiceLevel.SelfServe;
    private int productCount;
    @Setter
    @Getter
    private String cardType;
    @Setter
    @Getter
    private BigDecimal salesTax;
    @Setter
    @Getter
    private BigDecimal discount;
    private LinkedHashMap<String, DE63_ProductDataEntry> productDataEntries;
    @Getter@Setter
    private LinkedHashMap<String, DE63_ProductDataEntry> fuelProductDataEntries;
    @Getter@Setter
    private LinkedHashMap<String, DE63_ProductDataEntry> nonFuelProductDataEntries;
    private String EMPTY_STRING ="  ";
    private static final String VOYAGER_FLEET = "VoyagerFleet";
    private static final String MASTERCARD_FLEET = "MCFleet";
    private static final String VISA_FLEET = "VisaFleet";
    private static final String FUELMAN_FLEET = "FuelmanFleet";
    private static final String FLEETWIDE_FLEET = "FleetWide";
    private static final String FUEL_PRODUCT_COUNT_EXCEPTION = "Number of Fuel product should not more than 1";
    private static final String MASTERCARD_MISC_PRODUCT_CODE = "99";
    private static final String VISAFLEET_MISC_PRODUCT_CODE = "90";
    private static final String VOYAGER_MISC_PRODUCT_CODE = "33";
    private static final String FLEETCOR_MISC_PRODUCT_CODE = "400";
    private static final int FLEETCOR_MAX_PRODUCT_COUNT = 4;
    private static final int STANDARD_FLEET_MAX_PRODUCT_COUNT = 6;
    private static final Set<String> fleetCorCodes = fleetCorDiscountCodes();
    private static final Map<String, Integer> DISCOUNT_COUPON_PRIORITY = createDiscountCouponPriority();
    private static final Map<String, Set<String>> DISCOUNT_CODES_BY_FLEET_CARD = createDiscountCodesByFleetCard();

    private static Map<String, Set<String>> createDiscountCodesByFleetCard() {
        Map<String, Set<String>> discountCodesByFleetCard = new HashMap<>();
        discountCodesByFleetCard.put(VOYAGER_FLEET, Collections.unmodifiableSet(new HashSet<>(Arrays.asList("35", "36"))));
        discountCodesByFleetCard.put(FLEETWIDE_FLEET, fleetCorCodes);
        discountCodesByFleetCard.put(FUELMAN_FLEET, fleetCorCodes);
        return Collections.unmodifiableMap(discountCodesByFleetCard);
    }

    public ProductDataFormat getProductDataFormat() {
        return productDataFormat;
    }
    public void setProductDataFormat(ProductDataFormat productDataFormat) {
        this.productDataFormat = productDataFormat;
    }
    public ProductCodeSet getProductCodeSet() {
        return productCodeSet;
    }
    public void setProductCodeSet(ProductCodeSet productCodeSet) {
        this.productCodeSet = productCodeSet;
    }
    public ServiceLevel getServiceLevel() {
        return serviceLevel;
    }
    public void setServiceLevel(ServiceLevel serviceLevel) {
        this.serviceLevel = serviceLevel;
    }
    public int getProductCount() {
        return productDataEntries.size();
    }
    public int getFuelProductCount() {
        return fuelProductDataEntries.size();
    }
    public int getNonFuelProductCount() {
        return nonFuelProductDataEntries.size();
    }
    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
    public LinkedHashMap<String, DE63_ProductDataEntry> getProductDataEntries() {
        return productDataEntries;
    }
    public void setProductDataEntries(LinkedHashMap<String, DE63_ProductDataEntry> productDataEntries) {
        this.productDataEntries = productDataEntries;
    }

    public DE63_ProductData() {
        productDataEntries = new LinkedHashMap<String, DE63_ProductDataEntry>();
        fuelProductDataEntries = new LinkedHashMap<String, DE63_ProductDataEntry>();
        nonFuelProductDataEntries = new LinkedHashMap<String, DE63_ProductDataEntry>();
    }

    public void add(DE63_ProductDataEntry entry) {
        productDataEntries.put(entry.getCode(), entry);
    }
    public void addFuel(DE63_ProductDataEntry entry) {
        if (fuelProductDataEntries.containsKey(entry.getCode())) {
            DE63_ProductDataEntry existingEntry = fuelProductDataEntries.get(entry.getCode());
            existingEntry.setQuantity(existingEntry.getQuantity().add(entry.getQuantity()));
            existingEntry.setPrice(existingEntry.getPrice());
            existingEntry.setAmount(existingEntry.getAmount().add(entry.getAmount()));
        } else {
        fuelProductDataEntries.put(entry.getCode(), entry);
        }
    }

    public void addNonFuel(DE63_ProductDataEntry entry) {
        if (nonFuelProductDataEntries.containsKey(entry.getCode())) {
            DE63_ProductDataEntry existingEntry = nonFuelProductDataEntries.get(entry.getCode());
            existingEntry.setQuantity(existingEntry.getQuantity().add(entry.getQuantity()));
            existingEntry.setPrice(existingEntry.getPrice());
            existingEntry.setAmount(existingEntry.getAmount().add(entry.getAmount()));
        } else {
            nonFuelProductDataEntries.put(entry.getCode(), entry);
        }
    }
    public BigDecimal getFuelAmount() {
        BigDecimal sumAmount = BigDecimal.ZERO;
        for (DE63_ProductDataEntry fuelDataEntry : fuelProductDataEntries.values()) {
            sumAmount = sumAmount.add(fuelDataEntry.getAmount());
        }
        return sumAmount.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getFuelWithTax(){
        BigDecimal sumAmount = BigDecimal.ZERO;

        for (DE63_ProductDataEntry fuelDataEntry : fuelProductDataEntries.values()) {
            sumAmount = sumAmount.add(fuelDataEntry.getAmount());
        }
        if(salesTax!=null) {
            sumAmount = sumAmount.add(salesTax);
        }
        return sumAmount.setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal getNonFuelAmount(){
        BigDecimal sumAmount = BigDecimal.ZERO;
        for (DE63_ProductDataEntry nonFuelDataEntry : nonFuelProductDataEntries.values()) {
            sumAmount = sumAmount.add(nonFuelDataEntry.getAmount());
        }
        return sumAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getNonFuelWithTax(){
        BigDecimal sumAmount = BigDecimal.ZERO;

        for (DE63_ProductDataEntry nonFuelDataEntry : nonFuelProductDataEntries.values()) {
            sumAmount = sumAmount.add(nonFuelDataEntry.getAmount());
        }
        if(salesTax!=null) {
            sumAmount = sumAmount.add(salesTax);
        }
        return sumAmount.setScale(2, RoundingMode.HALF_UP);
    }


    public DE63_ProductData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        productDataFormat = sp.readStringConstant(1, ProductDataFormat.class);
        productCodeSet = sp.readStringConstant(1, ProductCodeSet.class);
        serviceLevel = sp.readStringConstant(1, ServiceLevel.class);

        switch(productDataFormat) {
            case GlobalPaymentsStandardFormat: {
                productCount = sp.readInt(3);
                for(int i = 0; i < productCount; i++) {
                    String code = sp.readToChar('\\');
                    String quantity = sp.readToChar('\\');
                    String price = sp.readToChar('\\');
                    String amount = sp.readToChar('\\');

                    DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
                    entry.setCode(code);
                    entry.setPrice(StringUtils.toFractionalAmount(price));
                    entry.setAmount(StringUtils.toAmount(amount));

                    if(!StringUtils.isNullOrEmpty(quantity)) {
                        entry.setUnitOfMeasure(ReverseStringEnumMap.parse(quantity.substring(0, 1), UnitOfMeasure.class));
                        entry.setQuantity(StringUtils.toFractionalAmount(quantity.substring(1)));
                    }

                    productDataEntries.put(code, entry);
                }
            } break;
            case ANSI_X9_TG23_Format: {
                productCount = sp.readInt(2);
                for(int i = 0; i < productCount; i++) {
                    String code = sp.readString(3);
                    String quantity = sp.readToChar('\\');
                    String price = sp.readToChar('\\');
                    String amount = sp.readToChar('\\');

                    DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
                    entry.setCode(code);
                    entry.setPrice(StringUtils.toFractionalAmount(price));
                    entry.setAmount(StringUtils.toAmount(amount));

                    if(!StringUtils.isNullOrEmpty(quantity)) {
                        entry.setUnitOfMeasure(ReverseStringEnumMap.parse(quantity.substring(0, 1), UnitOfMeasure.class));
                        entry.setQuantity(StringUtils.toFractionalAmount(quantity.substring(1)));
                    }

                    productDataEntries.put(code, entry);
                }
            } break;
            case GlobalPayments_ProductCoupon_Format: {
                productCount = sp.readInt(2);
                for(int i = 0; i < productCount; i++) {
                    ProductCodeSet set = sp.readStringConstant(1, ProductCodeSet.class);
                    String code = sp.readToChar('\\');
                    String quantity = sp.readToChar('\\');
                    String price = sp.readToChar('\\');
                    String amount = sp.readToChar('\\');
                    String couponStatus = sp.readToChar('\\');
                    String couponCode = sp.readToChar('\\');
                    String serialNumber = sp.readToChar('\\');

                    DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
                    entry.setCodeSet(set);
                    entry.setCode(code);
                    entry.setPrice(StringUtils.toFractionalAmount(price));
                    entry.setAmount(StringUtils.toAmount(amount));

                    if(!StringUtils.isNullOrEmpty(quantity)) {
                        entry.setUnitOfMeasure(ReverseStringEnumMap.parse(quantity.substring(0, 1), UnitOfMeasure.class));
                        entry.setQuantity(StringUtils.toFractionalAmount(quantity.substring(1)));
                    }

                    if(!StringUtils.isNullOrEmpty(couponStatus)) {
                        String status = couponStatus.substring(0, 1);
                        String markdownType = couponStatus.substring(1, 2);
                        BigDecimal value = StringUtils.toAmount(couponStatus.substring(2));

                        entry.setCouponStatus(status);
                        entry.setCouponMarkdownType(markdownType);
                        entry.setCouponValue(value);
                    }

                    if(!StringUtils.isNullOrEmpty(couponCode)) {
                        ProductCodeSet psc = ReverseStringEnumMap.parse(couponCode.substring(0, 1), ProductCodeSet.class);
                        couponCode = couponCode.substring(1);

                        entry.setCouponProductSetCode(psc);
                        entry.setCouponCode(couponCode);
                    }

                    entry.setCouponExtendedCode(serialNumber);

                    productDataEntries.put(code, entry);
                }
            } break;

            case VISAFLEET2Dot0: {
                productCount = sp.readInt(3);
                for(int i = 0; i < productCount; i++) {
                    String code = sp.readString(2);
                    String quantity = sp.readToChar('\\');
                    String price = sp.readToChar('\\');
                    String amount = sp.readToChar('\\');

                    DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
                    entry.setCode(code);
                    entry.setPrice(StringUtils.toFractionalAmount(price));
                    entry.setAmount(StringUtils.toAmount(amount));

                    if(!StringUtils.isNullOrEmpty(quantity)) {
                        entry.setUnitOfMeasure(ReverseStringEnumMap.parse(quantity.substring(0, 1), UnitOfMeasure.class));
                        entry.setQuantity(StringUtils.toFractionalAmount(quantity.substring(1)));
                    }
                    productDataEntries.put(code, entry);
                }
            } break;
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = productDataFormat.getValue()
                .concat(productCodeSet.getValue())
                .concat(serviceLevel.getValue());

        switch(productDataFormat) {
            case GlobalPaymentsStandardFormat: {
                if ((cardType != null) && ((cardType).equals(VOYAGER_FLEET))) {
                    rvalue = handleFleetFormat(rvalue, VOYAGER_MISC_PRODUCT_CODE, VOYAGER_FLEET);
                } else if ((cardType != null) && ((cardType).equals(MASTERCARD_FLEET))) {
                    rvalue = handleFleetFormat(rvalue, MASTERCARD_MISC_PRODUCT_CODE, MASTERCARD_FLEET);
                } else if ((cardType != null) && ((cardType).equals(VISA_FLEET))) {
                    rvalue = handleFleetFormat(rvalue, VISAFLEET_MISC_PRODUCT_CODE, VISA_FLEET);
                } else if ((cardType != null) && ((cardType).equals(FUELMAN_FLEET))) {
                    rvalue = handleFleetFormat(rvalue, FLEETCOR_MISC_PRODUCT_CODE, FUELMAN_FLEET);
                } else if ((cardType != null) && ((cardType).equals(FLEETWIDE_FLEET))) {
                    rvalue = handleFleetFormat(rvalue, FLEETCOR_MISC_PRODUCT_CODE, FLEETWIDE_FLEET);
                } else {
                    LinkedHashMap<String, DE63_ProductDataEntry> productDataCountEntries = new LinkedHashMap<>();
                    int count = 0;
                    if (getFuelProductCount() != 0 || getNonFuelProductCount() != 0) {
                        count = getFuelProductCount() + getNonFuelProductCount();
                        if (getFuelProductCount() != 0) {
                            productDataCountEntries = new LinkedHashMap<>(getFuelProductDataEntries());
                            productDataCountEntries.putAll(getNonFuelProductDataEntries());
                        } else {
                            productDataCountEntries = new LinkedHashMap<>(getNonFuelProductDataEntries());
                        }
                    } else if (getProductDataEntries().size() != 0) {
                        count = getProductCount();
                        productDataCountEntries = new LinkedHashMap<>(productDataEntries);

                    }
                    rvalue = rvalue.concat(StringUtils.padLeft(count, 3, '0'));
                    if (!productDataCountEntries.isEmpty()) {
                        for (DE63_ProductDataEntry entry : productDataCountEntries.values()) {
                            rvalue = rvalue.concat(entry.getCode() + "\\");

                            if (entry.getUnitOfMeasure() != null) {
                                rvalue = rvalue.concat(entry.getUnitOfMeasure().getValue());
                            }
                            if (entry.getQuantity() != null) {
                                rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
                            }
                            rvalue = rvalue.concat("\\")
                                    .concat(StringUtils.toFractionalNumeric(entry.getPrice()) + "\\")
                                    .concat(StringUtils.toNumeric(entry.getAmount()) + "\\");
                        }
                    }
                }
            }break;
            case ANSI_X9_TG23_Format: {
                rvalue = rvalue.concat(StringUtils.padLeft(getProductCount(), 2, '0'));
                for(DE63_ProductDataEntry entry: productDataEntries.values()) {
                    rvalue = rvalue.concat(entry.getCode());

                    if(entry.getUnitOfMeasure() != null) {
                        rvalue = rvalue.concat(entry.getUnitOfMeasure().getValue());
                    }
                    if(entry.getQuantity() != null) {
                        rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
                    }
                    rvalue = rvalue.concat("\\")
                            .concat(StringUtils.toFractionalNumeric(entry.getPrice()) + "\\")
                            .concat(StringUtils.toNumeric(entry.getAmount()) + "\\");
                }
            } break;
            case GlobalPayments_ProductCoupon_Format: {
                rvalue = rvalue.concat(StringUtils.padLeft(getProductCount(), 3, '0'));
                for(DE63_ProductDataEntry entry: productDataEntries.values()) {
                    rvalue = rvalue.concat(entry.getCode()+ "\\");

                    if(entry.getUnitOfMeasure() != null) {
                        rvalue = rvalue.concat(entry.getUnitOfMeasure().getValue());
                    }
                    if(entry.getQuantity() != null) {
                        rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
                    }
                    rvalue = rvalue.concat("\\")
                            .concat(StringUtils.toFractionalNumeric(entry.getPrice()) + "\\")
                            .concat(StringUtils.toNumeric(entry.getAmount()) + "\\")
                            .concat(entry.getCouponStatus())
                            .concat(entry.getCouponMarkdownType())
                            .concat(StringUtils.toNumeric(entry.getCouponValue()) + "\\")
                            .concat(entry.getCouponProductSetCode().getValue())
                            .concat(entry.getCouponCode() + "\\")
                            .concat(entry.getCouponExtendedCode() + "\\");
                }
            }
            break;
            case VISAFLEET2Dot0:
                if(getFuelProductCount()>1 ){
                    throw new UnsupportedOperationException("Number of Fuel product should not more than 1");
                }
                else {
                    int nonFuelCount = getNonFuelProductCount();
                    int encodedNonFuelCount = Math.min(nonFuelCount, 8);
                    int totalProductCount = getFuelProductCount() + encodedNonFuelCount;
                    rvalue = rvalue.concat(StringUtils.padLeft(totalProductCount, 3, '0'));

                    if (getFuelProductCount() == 1) {
                        for (DE63_ProductDataEntry entry : fuelProductDataEntries.values()) {
                            rvalue = rvalue.concat(StringUtils.padRight(entry.getCode(),4,' '));
                            rvalue = rvalue.concat("\\");

                            if (entry.getUnitOfMeasure() != null) {
                                rvalue = rvalue.concat(entry.getUnitOfMeasure().getValue());
                            }
                            if (entry.getQuantity() != null) {
                                rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));

                            }
                            rvalue = rvalue.concat("\\")
                                    .concat(StringUtils.toFractionalNumeric(entry.getPrice()) + "\\")
                                    .concat(StringUtils.toNumericWithPrecision(entry.getAmount(), 4) + "\\");
                        }
                    }
                    if (getNonFuelProductCount() != 0) {
                        rvalue = handleVisaFleetTwoDot0NonFuelEntries(rvalue, nonFuelCount);
                    }
                }
        }
        return rvalue.getBytes();
    }

    private String handleFleetFormat(String rvalue, String combinedProductCode, String fleetCardType) {
        // Fleet card doesn't support multi fuel
        if (getFuelProductCount() > 1) {
            throw new UnsupportedOperationException(FUEL_PRODUCT_COUNT_EXCEPTION);
        }

        int maxProductCount = (FLEETWIDE_FLEET.equals(fleetCardType) || FUELMAN_FLEET.equals(fleetCardType)) ? FLEETCOR_MAX_PRODUCT_COUNT : STANDARD_FLEET_MAX_PRODUCT_COUNT;

        Set<String> discountCodes = getDiscountCodesForFleetCard(fleetCardType);
        LinkedHashMap<String, DE63_ProductDataEntry> decreasingOrderNonFuelEntries = getDecreasingOrderNonFuelEntries();
        LinkedHashMap<String, DE63_ProductDataEntry> nonDiscountNonFuelEntries = getFilteredNonFuelEntries(decreasingOrderNonFuelEntries, discountCodes, false);
        LinkedHashMap<String, DE63_ProductDataEntry> discountProducts = getFilteredNonFuelEntries(decreasingOrderNonFuelEntries, discountCodes, true);

        // Total encoded count includes fuel + non-discount non-fuel + discounts, capped at max.
        int totalProductCount = getFuelProductCount() + nonDiscountNonFuelEntries.size() + discountProducts.size();
        int encodedProductCount = Math.min(totalProductCount, maxProductCount);
        String result = rvalue.concat(StringUtils.padLeft(encodedProductCount, 3, '0'));

        if (getFuelProductCount() != 0) {
            for (DE63_ProductDataEntry entry : fuelProductDataEntries.values()) {
                result = appendProductEntry(result, entry);
            }
        }

        int availableSlots = maxProductCount - getFuelProductCount();
        int nonFuelCount = nonDiscountNonFuelEntries.size();
        // Rollup is required when non-discount non-fuel AND discount entries together exceed available slots.
        boolean isRollupRequired = (nonFuelCount + discountProducts.size()) > availableSlots;

        if (!isRollupRequired) {
            // In non-rollup mode, place discount entries at the tail (before any potential rollup slot).
            for (DE63_ProductDataEntry entry : nonDiscountNonFuelEntries.values()) {
                result = appendProductEntry(result, entry);
            }

            for (DE63_ProductDataEntry entry : getDiscountEntriesInOrder(discountProducts)) {
                result = appendProductEntry(result, entry);
            }
        } else if (!nonDiscountNonFuelEntries.isEmpty()) {
                String unitOfMeasure = "";
                BigDecimal combinedQuantity = BigDecimal.ZERO;
                BigDecimal combinedAmount = BigDecimal.ZERO;
                int slotsBeforeRollup = Math.max(availableSlots - 1, 0);
                List<DE63_ProductDataEntry> discountEntries = getDiscountEntriesInOrder(discountProducts);
                int discountSlotsBeforeRollup = Math.min(discountEntries.size(), slotsBeforeRollup);
                int nonDiscountSlotsBeforeRollup = Math.max(slotsBeforeRollup - discountSlotsBeforeRollup, 0);
                int processedCount = 0;

                for (DE63_ProductDataEntry entry : nonDiscountNonFuelEntries.values()) {
                    if (processedCount < nonDiscountSlotsBeforeRollup) {
                        result = appendProductEntry(result, entry);
                        processedCount++;
                    } else {
                        if (StringUtils.isNullOrEmpty(unitOfMeasure) && entry.getUnitOfMeasure() != null) {
                            unitOfMeasure = entry.getUnitOfMeasure().getValue();
                        }
                        if (entry.getQuantity() != null) {
                            combinedQuantity = combinedQuantity.add(entry.getQuantity());
                        }
                        combinedAmount = combinedAmount.add(entry.getAmount());
                    }
                }

                // When rollup is used, card-specific discount entries must appear immediately before rollup.
                for (int i = 0; i < discountSlotsBeforeRollup; i++) {
                    result = appendProductEntry(result, discountEntries.get(i));
                }

                result = result.concat(combinedProductCode + "\\");
                if (!unitOfMeasure.isEmpty()) {
                    result = result.concat(unitOfMeasure);
                }
                result = result.concat(StringUtils.toFractionalNumeric(combinedQuantity));
                result = result.concat("\\")
                        .concat("\\")
                        .concat(StringUtils.toNumeric(combinedAmount) + "\\");
        } else {
            // Rollup required with discount-only products; emit discounts directly up to available slots.
            List<DE63_ProductDataEntry> discountEntries = getDiscountEntriesInOrder(discountProducts);
            int discountSlots = Math.min(discountEntries.size(), availableSlots);
            for (int i = 0; i < discountSlots; i++) {
                result = appendProductEntry(result, discountEntries.get(i));
            }
        }

        return result;
    }

    private String appendProductEntry(String rvalue, DE63_ProductDataEntry entry) {
        rvalue = rvalue.concat(entry.getCode() + "\\");

        if (entry.getUnitOfMeasure() != null) {
            rvalue = rvalue.concat(entry.getUnitOfMeasure().getValue());
        }
        if (entry.getQuantity() != null) {
            rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
        }

        rvalue = rvalue.concat("\\");
        rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getPrice()));

        return rvalue.concat("\\")
                .concat(StringUtils.toNumeric(entry.getAmount()) + "\\");
    }

    private LinkedHashMap<String, DE63_ProductDataEntry> getDecreasingOrderNonFuelEntries() {
        LinkedHashMap<String, DE63_ProductDataEntry> decreasingOrderNonFuelEntries = nonFuelProductDataEntries.entrySet()
                .stream()
                // Sort by total amount (descending), not unit price.
                .sorted(Map.Entry.comparingByValue((entry1, entry2) -> entry2.getAmount().compareTo(entry1.getAmount())))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
        return decreasingOrderNonFuelEntries;
    }

    private String handleVisaFleetTwoDot0NonFuelEntries(String rvalue, int nonFuelCount) {
        LinkedHashMap<String, DE63_ProductDataEntry> decreasingOrderNonFuelEntries = getDecreasingOrderNonFuelEntries();

        // Separate discount/coupon entries from regular entries
        List<DE63_ProductDataEntry> discountCouponEntries = new ArrayList<>();
        List<DE63_ProductDataEntry> regularEntries = new ArrayList<>();
        for (DE63_ProductDataEntry entry : decreasingOrderNonFuelEntries.values()) {
            if (isDiscountOrCoupon(entry)) {
                discountCouponEntries.add(entry);
            } else {
                regularEntries.add(entry);
            }
        }

        // Sort discount/coupon entries by priority
        discountCouponEntries.sort(Comparator.comparingInt(
                e -> DISCOUNT_COUPON_PRIORITY.getOrDefault(e.getCode(), DISCOUNT_COUPON_PRIORITY.size())
        ));

        if (nonFuelCount <= 8) {
            // Write all regular entries first
            for (DE63_ProductDataEntry entry : regularEntries) {
                rvalue = appendVisaFleetTwoDot0Entry(rvalue, entry);
            }
            // Write discount/coupon entries at the end
            for (DE63_ProductDataEntry entry : discountCouponEntries) {
                rvalue = appendVisaFleetTwoDot0Entry(rvalue, entry);
            }
        } else {
            // Reserve slots for discount/coupon entries and the ZC combined entry
            int reservedSlots = discountCouponEntries.size() + 1; // +1 for ZC
            int regularSlots = 8 - reservedSlots;

            BigDecimal combinedQuantity = BigDecimal.ZERO;
            BigDecimal combinedAmount = BigDecimal.ZERO;
            int processedCount = 0;

            for (DE63_ProductDataEntry entry : regularEntries) {
                if (processedCount < regularSlots) {
                    rvalue = appendVisaFleetTwoDot0Entry(rvalue, entry);
                    processedCount++;
                } else {
                    if (entry.getQuantity() != null) {
                        combinedQuantity = combinedQuantity.add(entry.getQuantity());
                    }
                    combinedAmount = combinedAmount.add(entry.getAmount());
                }
            }

            // Write discount/coupon entries at the end
            for (DE63_ProductDataEntry entry : discountCouponEntries) {
                rvalue = appendVisaFleetTwoDot0Entry(rvalue, entry);
            }

            // Write combined remaining entry with ZC code
            rvalue = rvalue.concat("ZC\\");
            rvalue = rvalue.concat(" ");
            rvalue = rvalue.concat(StringUtils.toFractionalNumeric(combinedQuantity));
            rvalue = rvalue.concat("\\")
                    .concat("\\")
                    .concat(StringUtils.toNumericWithPrecision(combinedAmount, 4) + "\\");
        }

        return rvalue;
    }

    private String appendVisaFleetTwoDot0Entry(String rvalue, DE63_ProductDataEntry entry) {
        rvalue = rvalue.concat(entry.getCode() + "\\");
        rvalue = rvalue.concat(" "); // Unit Of Measure
        if (entry.getQuantity() != null) {
            rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
        }
        rvalue = rvalue.concat("\\")
                .concat("\\") // Price
                .concat(StringUtils.toNumericWithPrecision(entry.getAmount(), 4) + "\\");
        return rvalue;
    }

    private static Map<String, Integer> createDiscountCouponPriority() {
        List<ProductCode> codes = Arrays.asList(
                ProductCode.DISCOUNT_CODE1, ProductCode.DISCOUNT_CODE2, ProductCode.DISCOUNT_CODE3,
                ProductCode.DISCOUNT_CODE4, ProductCode.DISCOUNT_CODE5,
                ProductCode.TAX_DISCOUNT_OR_FORGIVEN,
                ProductCode.LOCAL_DISCOUNT_CODE1, ProductCode.LOCAL_DISCOUNT_CODE2, ProductCode.LOCAL_DISCOUNT_CODE3,
                ProductCode.LOCAL_DISCOUNT_CODE4, ProductCode.LOCAL_DISCOUNT_CODE5,
                ProductCode.POS_OR_LOYALTY_RESERVED_DISCOUNT_CODE1, ProductCode.POS_OR_LOYALTY_RESERVED_DISCOUNT_CODE2,
                ProductCode.POS_OR_LOYALTY_RESERVED_DISCOUNT_CODE3, ProductCode.POS_OR_LOYALTY_RESERVED_DISCOUNT_CODE4,
                ProductCode.POS_OR_LOYALTY_RESERVED_DISCOUNT_CODE5,
                ProductCode.COUPONS_CODE1, ProductCode.COUPONS_CODE2, ProductCode.COUPONS_CODE3,
                ProductCode.COUPONS_CODE4, ProductCode.COUPONS_CODE5
        );
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < codes.size(); i++) {
            map.put(codes.get(i).getValue(), i);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Set<String> fleetCorDiscountCodes() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                FleetCorConexxusProductCode.DISCOUNT_1.getValue(),
                FleetCorConexxusProductCode.DISCOUNT_2.getValue(),
                FleetCorConexxusProductCode.DISCOUNT_3.getValue(),
                FleetCorConexxusProductCode.DISCOUNT_4.getValue(),
                FleetCorConexxusProductCode.DISCOUNT_5.getValue(),
                FleetCorConexxusProductCode.COUPON_1.getValue(),
                FleetCorConexxusProductCode.COUPON_2.getValue(),
                FleetCorConexxusProductCode.COUPON_3.getValue(),
                FleetCorConexxusProductCode.COUPON_4.getValue(),
                FleetCorConexxusProductCode.COUPON_5.getValue(),
                FleetCorConexxusProductCode.LOTTERY_PAY_OUT_INSTANT.getValue(),
                FleetCorConexxusProductCode.LOTTERY_PAY_OUT_ONLINE.getValue(),
                FleetCorConexxusProductCode.LOTTERY_PAY_OUT_OTHER.getValue(),
                FleetCorConexxusProductCode.SPLIT_TENDER.getValue(),
                FleetCorConexxusProductCode.TAX_DISCOUNT_FORGIVEN.getValue(),
                FleetCorConexxusProductCode.MISCELLANEOUS_NEGATIVE_ADMINISTRATIVE.getValue()
        )));
    }

    private boolean isDiscountOrCoupon(DE63_ProductDataEntry entry) {
        return DISCOUNT_COUPON_PRIORITY.containsKey(entry.getCode());
    }

    private Set<String> getDiscountCodesForFleetCard(String fleetCardType) {
        return DISCOUNT_CODES_BY_FLEET_CARD.getOrDefault(fleetCardType, Collections.<String>emptySet());
    }

    private LinkedHashMap<String, DE63_ProductDataEntry> getFilteredNonFuelEntries(
            LinkedHashMap<String, DE63_ProductDataEntry> sourceEntries,
            Set<String> discountCodes,
            boolean includeDiscountEntries
    ) {
        LinkedHashMap<String, DE63_ProductDataEntry> filteredEntries = new LinkedHashMap<>();

        for (Map.Entry<String, DE63_ProductDataEntry> entry : sourceEntries.entrySet()) {
            boolean isDiscountEntry = discountCodes.contains(entry.getValue().getCode());
            if (isDiscountEntry == includeDiscountEntries) {
                filteredEntries.put(entry.getKey(), entry.getValue());
            }
        }

        return filteredEntries;
    }

    private List<DE63_ProductDataEntry> getDiscountEntriesInOrder(LinkedHashMap<String, DE63_ProductDataEntry> discountProducts) {
        List<DE63_ProductDataEntry> discountEntries = new ArrayList<>(discountProducts.values());
        discountEntries.sort(Comparator.comparing(DE63_ProductDataEntry::getCode));
        return discountEntries;
    }

    public String toString() {
        return new String(toByteArray());
    }
}
