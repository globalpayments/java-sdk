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
    private LinkedHashMap<String, DE63_ProductDataEntry> productDataEntries;
    @Getter@Setter
    private LinkedHashMap<String, DE63_ProductDataEntry> fuelProductDataEntries;
    @Getter@Setter
    private LinkedHashMap<String, DE63_ProductDataEntry> nonFuelProductDataEntries;
    private String EMPTY_STRING ="  ";

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

        public void add(BigDecimal salesTax) {
        setSalesTax(salesTax);
    }

    public BigDecimal getFuelAmount() {
        BigDecimal sumAmount = BigDecimal.ZERO;
        for (DE63_ProductDataEntry fuelDataEntry : fuelProductDataEntries.values()) {
            sumAmount = sumAmount.add(fuelDataEntry.getAmount());
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
                    rvalue = rvalue.concat(StringUtils.padLeft(getProductCount(), 3, '0'));
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
                if(getFuelProductCount()>1 || getNonFuelProductCount()>8){
                    throw new UnsupportedOperationException("Number of Fuel/NonFuel product should not more than 1 and 8");
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
                    if (getNonFuelProductCount() != 0 && getNonFuelProductCount() < 8) {
                        for (DE63_ProductDataEntry entry : nonFuelProductDataEntries.values()) {
                            rvalue = rvalue.concat(entry.getCode() + "\\")
                                    .concat("\\")   //Quantity
                                    .concat("\\")   //Price
                                    .concat("\\"); //Amount
                        }
                    }
                    break;
                }
        }
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
