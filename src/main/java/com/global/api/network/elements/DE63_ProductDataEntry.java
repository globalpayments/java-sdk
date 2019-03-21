package com.global.api.network.elements;

import com.global.api.network.enums.ProductCodeSet;
import com.global.api.network.enums.UnitOfMeasure;

import java.math.BigDecimal;

public class DE63_ProductDataEntry {
    private ProductCodeSet codeSet;
    private String code;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;

    private String couponStatus;
    private String couponMarkdownType;
    private BigDecimal couponValue;
    private ProductCodeSet couponProductSetCode;
    private String couponCode;
    private String couponExtendedCode;

    public ProductCodeSet getCodeSet() {
        return codeSet;
    }
    public void setCodeSet(ProductCodeSet codeSet) {
        this.codeSet = codeSet;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCouponStatus() {
        return couponStatus;
    }
    public void setCouponStatus(String couponStatus) {
        this.couponStatus = couponStatus;
    }
    public String getCouponMarkdownType() {
        return couponMarkdownType;
    }
    public void setCouponMarkdownType(String couponMarkdownType) {
        this.couponMarkdownType = couponMarkdownType;
    }
    public BigDecimal getCouponValue() {
        return couponValue;
    }
    public void setCouponValue(BigDecimal couponValue) {
        this.couponValue = couponValue;
    }
    public ProductCodeSet getCouponProductSetCode() {
        return couponProductSetCode;
    }
    public void setCouponProductSetCode(ProductCodeSet couponProductSetCode) {
        this.couponProductSetCode = couponProductSetCode;
    }
    public String getCouponCode() {
        return couponCode;
    }
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    public String getCouponExtendedCode() {
        return couponExtendedCode;
    }
    public void setCouponExtendedCode(String couponExtendedCode) {
        this.couponExtendedCode = couponExtendedCode;
    }
}
