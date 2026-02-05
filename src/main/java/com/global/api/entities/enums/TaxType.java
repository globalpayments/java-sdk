package com.global.api.entities.enums;

public enum TaxType implements IStringConstant {
    NotUsed("NOTUSED"),
    SalesTax("SALES_TAX"),
    TaxExempt("TAXEXEMPT");

    String value;
    TaxType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
