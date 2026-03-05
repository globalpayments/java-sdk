package com.global.api.entities.enums;

public enum TaxType implements IStringConstant {
    NotUsed("NOTUSED"),
    SalesTax("SALESTAX"),
    TaxExempt("TAXEXEMPT"),
    Not_Used("NOT_USED"),
    Sales_Tax("SALES_TAX"),
    Tax_Exempt("TAX_EXEMPT");

    String value;
    TaxType(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
