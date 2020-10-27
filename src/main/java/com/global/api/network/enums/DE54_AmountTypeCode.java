package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE54_AmountTypeCode implements IStringConstant {
    AccountLedgerBalance("01"),
    AccountAvailableBalance("02"),
    AmountCash("40"),
    AmountGoodsAndServices("41"),
    WIC_GenericDiscountAmount("52"),
    AmountTax("56"),
    AmountDiscount("57"),
    AmountFundsForDeposit("58"),
    AmountInvoice("59"),
    WIC_CommissaryFee("60"),
    WIC_VolumeDiscount("61"),
    WIC_VendorLoyaltyDiscount("62"),
    Coupon("63"),
    AmountGratuity("90"),
    RetailAmount("91"),
    WholesaleAmount("92"),
    LastPaymentAmount("93"),
    ShadowLimit("94"),
    MiscTax_1("95"),
    MiscTax_2("96");

    private final String value;
    DE54_AmountTypeCode(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
