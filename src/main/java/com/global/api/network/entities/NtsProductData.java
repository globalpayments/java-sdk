package com.global.api.network.entities;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE63_ProductDataEntry;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.IPaymentMethod;
import com.global.api.utils.NtsUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NtsProductData {
    @Setter
    @Getter
    private ServiceLevel serviceLevel = ServiceLevel.SelfServe;
    @Setter
    @Getter
    private List<DE63_ProductDataEntry> nonFuelDataEntries;
    @Setter
    @Getter
    private List<DE63_ProductDataEntry> fuelDataEntries;
    @Setter
    @Getter
    private BigDecimal salesTax;
    @Setter
    @Getter
    private BigDecimal discount;
    @Setter
    @Getter
    private ProductCodeType productCodeType;
    @Setter
    @Getter
    private int promptValue;
    @Setter
    @Getter
    private PurchaseType purchaseType;
    @Getter
    @Setter
    private NTSCardTypes cardType;
    @Getter @Setter
    private BigDecimal netFuelAmount;
    @Getter @Setter
    private BigDecimal grossNonFuelAmount;
    @Getter @Setter
    private BigDecimal netNonFuelAmount;
    @Getter @Setter
    private String nonFuelProductCount;

    public NtsProductData() {
        nonFuelDataEntries = new ArrayList<>();
        fuelDataEntries = new ArrayList<>();
    }

    public NtsProductData(ServiceLevel serviceLevel) {
        this();
        setServiceLevel(serviceLevel);
    }

    public NtsProductData(ServiceLevel serviceLevel, IPaymentMethod cardType) {
        this();
        setServiceLevel(serviceLevel);
        setCardType(NtsUtils.mapCardType(cardType));
    }

    public void add(BigDecimal salesTax) {
        setSalesTax(salesTax);
    }

    public void add(BigDecimal salesTax, BigDecimal discount) {
        setSalesTax(salesTax);
        setDiscount(discount);
    }

   public void addToNonFuelList(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
        entry.setCode(productCode);
        entry.setUnitOfMeasure(unitOfMeasure);
        entry.setQuantity(quantity);
        entry.setPrice(price);
        entry.setAmount(amount);
        getNonFuelDataEntries().add(entry);
    }

    public void addToFuelList(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
        entry.setCode(productCode);
        entry.setUnitOfMeasure(unitOfMeasure);
        entry.setQuantity(quantity);
        entry.setPrice(price);
        entry.setAmount(amount);
        this.getFuelDataEntries().add(entry);
    }


    /**
     * Get the product code based on the card type.
     *
     * @param code
     * @return
     */
    private String getProductCode(NtsProductCode code) throws ApiException {
        if (cardType == null) {
            throw new ApiException("Card Type should be provided...");
        } else if (cardType == NTSCardTypes.MastercardFleet
                || cardType == NTSCardTypes.MastercardPurchasing
                || cardType == NTSCardTypes.VoyagerFleet
                || cardType == NTSCardTypes.VisaFleet) {
            return code.getProductCodeByCard(cardType);
        }
        return code.getValue();
    }


    public BigDecimal getCalculateTotalAmount() {

        BigDecimal sumAmount = BigDecimal.ZERO;

        for (DE63_ProductDataEntry fuelDataEntry : fuelDataEntries) {
            sumAmount = sumAmount.add(fuelDataEntry.getAmount());
        }
        for (DE63_ProductDataEntry nonFuelDataEntry : nonFuelDataEntries) {
            sumAmount = sumAmount.add(nonFuelDataEntry.getAmount());
        }
        return sumAmount.setScale(2, RoundingMode.HALF_UP);
    }


    /* with double values */
    public void addFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, double quantity, double price) throws ApiException {
        addToFuelList(getProductCode(ntsProductCode), unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(quantity * price));
    }

    public void addNonFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, double quantity, double price) throws ApiException {
        addToNonFuelList(getProductCode(ntsProductCode), unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(quantity * price));
    }

    public void addFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, double quantity, double price, double amount) throws ApiException {
        addToFuelList(getProductCode(ntsProductCode), unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(amount));
    }

    public void addNonFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, double quantity, double price, double amount) throws ApiException {
        addToNonFuelList(getProductCode(ntsProductCode), unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(amount));
    }

    /* with BigDecimal values */
    public void addFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price) throws ApiException {
        addToFuelList(getProductCode(ntsProductCode), unitOfMeasure, quantity, price, quantity.multiply(price));
    }

    public void addNonFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price) throws ApiException {
        addToNonFuelList(getProductCode(ntsProductCode), unitOfMeasure, quantity, price, quantity.multiply(price));
    }

    public void addFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) throws ApiException {
        addToFuelList(getProductCode(ntsProductCode), unitOfMeasure, quantity, price, amount);
    }

    public void addNonFuel(NtsProductCode ntsProductCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) throws ApiException {
        addToNonFuelList(getProductCode(ntsProductCode), unitOfMeasure, quantity, price, amount);
    }

    /* with string product codes */
    public void addFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addToFuelList(productCode, unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(quantity * price));
    }

    public void addFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price, double amount) {
        addToFuelList(productCode, unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(amount));
    }

    public void addNonFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price, double amount) {
        addToNonFuelList(productCode, unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(amount));
    }

    public void addNonFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addToNonFuelList(productCode, unitOfMeasure, BigDecimal.valueOf(quantity), BigDecimal.valueOf(price), BigDecimal.valueOf(quantity * price));
    }

}
