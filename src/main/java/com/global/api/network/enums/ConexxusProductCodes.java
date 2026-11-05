package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum ConexxusProductCodes implements IStringConstant {
    DISCOUNT_1("900"),
    DISCOUNT_2("901"),
    DISCOUNT_3("902"),
    DISCOUNT_4("903"),
    DISCOUNT_5("904"),
    COUPON_1("905"),
    COUPON_2("906"),
    COUPON_3("907"),
    COUPON_4("908"),
    COUPON_5("909"),
    LOTTERY_PAY_OUT_INSTANT("910"),
    LOTTERY_PAY_OUT_ONLINE("911"),
    LOTTERY_PAY_OUT_OTHER("912"),
    SPLIT_TENDER("913"),
    TAX_DISCOUNT_FORGIVEN("914"),
    MISCELLANEOUS_NEGATIVE_ADMINISTRATIVE("949"),
    /**
     * Tax , Fee & all added to the transaction as a positive amount.
     */
    TAX_1("950"),
    TAX_2("951"),
    TAX_3("952"),
    TAX_4("953"),
    TAX_5("954"),
    FEE_1("957"),
    FEE_2("958"),
    FEE_3("959"),
    FEE_4("960"),
    FEE_5("961"),
    MISCELLANEOUS_AVIATION_TAX("962"),
    GST_HST_VAT_1("963"),
    PST_QST_VAT_2("964"),
    SWT_RATE("965"),
    UNIDENTIFIED_ADMINISTRATIVE("966"),
    MISCELLANEOUS_ADMINISTRATIVE("999");

    private final String value;

    ConexxusProductCodes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}


