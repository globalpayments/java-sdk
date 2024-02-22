package com.global.api.network.entities;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.elements.DE63_ProductData;
import com.global.api.network.elements.DE63_ProductDataEntry;
import com.global.api.network.enums.*;

import java.math.BigDecimal;

public class ProductData {
    private DE63_ProductData productData;

    public ProductData(ServiceLevel serviceLevel) {
        this(serviceLevel, ProductCodeSet.Heartland);
    }

    public ProductData(ServiceLevel serviceLevel, ProductCodeSet productCodeSet) {
        productData = new DE63_ProductData();
        productData.setProductDataFormat(ProductDataFormat.HeartlandStandardFormat);
        productData.setProductCodeSet(productCodeSet);
        productData.setServiceLevel(serviceLevel);
    }

    public ProductData(ServiceLevel serviceLevel, ProductCodeSet productCodeSet, ProductDataFormat productDataFormat) {
        productData = new DE63_ProductData();
        productData.setProductDataFormat(productDataFormat);
        productData.setProductCodeSet(productCodeSet);
        productData.setServiceLevel(serviceLevel);
    }

    public void addSaleTax(BigDecimal sale) {
        productData.add(sale);
    }

    public void add(ProductCode productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        add(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void add(ProductCode productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        add(productCode.getValue(), unitOfMeasure, quantity, price, amount);
    }

    public void add(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        add(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void add(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
        entry.setCode(productCode);
        entry.setUnitOfMeasure(unitOfMeasure);
        entry.setQuantity(quantity);
        entry.setPrice(price);
        entry.setAmount(amount);
        productData.add(entry);
    }

    //Add Fuel
    public void addFuel(ProductCode productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addFuel(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void addFuel(ProductCode productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        addFuel(productCode.getValue(), unitOfMeasure, quantity, price, amount);
    }

    public void addFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addFuel(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void addFuel(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price) {
        addFuel(productCode, unitOfMeasure, quantity, price, quantity.multiply(price));
    }

    public void addFuel(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
        entry.setCode(productCode);
        entry.setUnitOfMeasure(unitOfMeasure);
        entry.setQuantity(quantity);
        entry.setPrice(price);
        entry.setAmount(amount);
        productData.addFuel(entry);
    }

    //Add NonFuel
    public void addNonFuel(ProductCode productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addNonFuel(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void addNonFuel(ProductCode productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        addNonFuel(productCode.getValue(), unitOfMeasure, quantity, price, amount);
    }

    public void addNonFuel(String productCode, UnitOfMeasure unitOfMeasure, double quantity, double price) {
        addNonFuel(productCode, unitOfMeasure, new BigDecimal(quantity), new BigDecimal(price), new BigDecimal(quantity * price));
    }

    public void addNonFuel(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price) {
        addNonFuel(productCode, unitOfMeasure, quantity, price, quantity.multiply(price));
    }

    public void addNonFuel(String productCode, UnitOfMeasure unitOfMeasure, BigDecimal quantity, BigDecimal price, BigDecimal amount) {
        DE63_ProductDataEntry entry = new DE63_ProductDataEntry();
        entry.setCode(productCode);
        entry.setUnitOfMeasure(unitOfMeasure);
        entry.setQuantity(quantity);
        entry.setPrice(price);
        entry.setAmount(amount);
        productData.addNonFuel(entry);
    }

    public DE63_ProductData toDataElement() {
        return productData;
    }
}
