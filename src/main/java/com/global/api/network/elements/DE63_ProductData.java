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
    private ProductDataFormat productDataFormat = ProductDataFormat.HeartlandStandardFormat;
    private ProductCodeSet productCodeSet = ProductCodeSet.Heartland;
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
    private static final String FUEL_PRODUCT_COUNT_EXCEPTION = "Number of Fuel product should not more than 1";

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
        fuelProductDataEntries.put(entry.getCode(), entry);
    }
    public void addNonFuel(DE63_ProductDataEntry entry) {
        nonFuelProductDataEntries.put(entry.getCode(), entry);
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
        return sumAmount.setScale(2, RoundingMode.HALF_UP);
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
            case HeartlandStandardFormat: {
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
            case Heartland_ProductCoupon_Format: {
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
            case HeartlandStandardFormat: {
                if ((cardType != null) && ((cardType).equals(VOYAGER_FLEET))) {
                    if (getFuelProductCount() > 1) {
                        throw new UnsupportedOperationException(FUEL_PRODUCT_COUNT_EXCEPTION);
                    } else {
                        int totalProductCount = getFuelProductCount() + getNonFuelProductCount();
                        if(totalProductCount>6){
                            rvalue = rvalue.concat(StringUtils.padLeft(6, 3, '0'));
                        }else{
                            rvalue = rvalue.concat(StringUtils.padLeft(totalProductCount, 3, '0'));
                        }
                        if (getFuelProductCount() != 0) {
                            for (DE63_ProductDataEntry entry : fuelProductDataEntries.values()) {
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
                        if (getNonFuelProductCount() != 0) {
                            String rvalue2 = (33 + "\\"); //Misc code for VoyagerFleet
                            String unitOfMeasure = "";
                            BigDecimal quantRollup = new BigDecimal(0);
                            BigDecimal priceRollup = new BigDecimal(0);
                            BigDecimal amountRollup = new BigDecimal(0);
                            int i = 1;
                            int rollUpCutOff ;
                            if (getFuelProductCount() == 0) {
                                rollUpCutOff = 6;
                            } else {
                                rollUpCutOff = 5;
                            }
                            LinkedHashMap<String, DE63_ProductDataEntry> decreasingOrderNonFuelEntries = getDecreasingOrderNonFuelEntries();
                            for (DE63_ProductDataEntry entry : decreasingOrderNonFuelEntries.values()) {
                                if (i < rollUpCutOff) {
                                    i=i+1;
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
                                } else {
                                    if (totalProductCount == 6) {
                                        rvalue2 = entry.getCode();
                                        rvalue2 = rvalue2.concat("\\");
                                    }
                                    if (entry.getUnitOfMeasure() != null) {
                                        unitOfMeasure = entry.getUnitOfMeasure().getValue();
                                    }
                                    if (entry.getQuantity() != null) {
                                        quantRollup = quantRollup.add(entry.getQuantity());
                                    }
                                    priceRollup = priceRollup.add(entry.getPrice());
                                    amountRollup = amountRollup.add(entry.getAmount());
                                    i=i+1;
                                }

                            }
                            if (i>rollUpCutOff) {
                                if (unitOfMeasure != null) {
                                    rvalue2 = rvalue2.concat(unitOfMeasure);
                                }

                                rvalue2 = rvalue2.concat(StringUtils.toFractionalNumeric(quantRollup));
                                rvalue2 = rvalue2.concat("\\")
                                        .concat(StringUtils.toFractionalNumeric(priceRollup) + "\\")
                                        .concat(StringUtils.toNumeric(amountRollup) + "\\");

                                rvalue = rvalue.concat(rvalue2);
                            }
                        }
                    }
                }
                else {
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
                    rvalue = rvalue.concat(StringUtils.padLeft(getProductCount(), 3, '0'));
                    if (productDataCountEntries.size() != 0) {
                        for (DE63_ProductDataEntry entry : productDataEntries.values()) {
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
            case Heartland_ProductCoupon_Format: {
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
                    int totalProductCount = getFuelProductCount() + getNonFuelProductCount();
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
                                    .concat(StringUtils.toNumeric(entry.getAmount()) + "\\");
                        }
                    }
                    if (getNonFuelProductCount() != 0) {
                        int nonFuelCount = getNonFuelProductCount();
                        int index = 0;
                        int rollup = Math.min(nonFuelCount, 8) - 1;
                        BigDecimal combinedQuantity = BigDecimal.ZERO;
                        BigDecimal combinedAmount = BigDecimal.ZERO;

                        for (DE63_ProductDataEntry entry : nonFuelProductDataEntries.values()) {
                            if (index < rollup || nonFuelCount <= 8) {
                                rvalue = rvalue.concat(entry.getCode() + "\\");
                                rvalue = rvalue.concat(" "); // Unit Of Measure

                                if (entry.getQuantity() != null) {
                                    rvalue = rvalue.concat(StringUtils.toFractionalNumeric(entry.getQuantity()));
                                }
                                rvalue = rvalue.concat("\\")
                                        .concat("\\") // Price
                                        .concat(StringUtils.toNumeric(entry.getAmount()) + "\\");
                            } else {
                                // Combine remaining products
                                if (entry.getQuantity() != null) {
                                    combinedQuantity = combinedQuantity.add(entry.getQuantity());
                                }
                                combinedAmount = combinedAmount.add(entry.getAmount());
                            }
                            index++;
                        }

                        // Add combined product as the 8th product if there are more than 8 products
                        if (nonFuelCount > 8) {
                            rvalue = rvalue.concat("ZC\\"); // Combined product code
                            rvalue = rvalue.concat(" "); // Unit Of Measure
                            rvalue = rvalue.concat(StringUtils.toFractionalNumeric(combinedQuantity));
                            rvalue = rvalue.concat("\\")
                                    .concat("\\") // Price
                                    .concat(StringUtils.toNumeric(combinedAmount) + "\\");
                        }
                    }
                }
        }
        return rvalue.getBytes();
    }

    private LinkedHashMap<String, DE63_ProductDataEntry> getDecreasingOrderNonFuelEntries() {
        LinkedHashMap<String, DE63_ProductDataEntry> decreasingOrderNonFuelEntries = nonFuelProductDataEntries.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((entry1, entry2) -> entry2.getPrice().compareTo(entry1.getPrice())))
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
        return decreasingOrderNonFuelEntries;
    }

    public String toString() {
        return new String(toByteArray());
    }
}
