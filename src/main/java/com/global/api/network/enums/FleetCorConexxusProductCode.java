package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum FleetCorConexxusProductCode implements IStringConstant {
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
    MISCELLANEOUS_NEGATIVE_ADMINISTRATIVE("949");

    private final String value;

    FleetCorConexxusProductCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public byte[] getBytes() {
        return value.getBytes();
    }
}


