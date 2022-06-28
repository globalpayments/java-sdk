package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum CardBrand implements IStringConstant {
    Unknown("Unknown"),
    Mastercard("Mastercard"),
    Visa("Visa"),
    AmericanExpress("Amex"),
    Discover("Discover"),
    JCB("Jcb"),
    Interac("Interac"),
    UnionPay("UnionPay");

    String value;

    CardBrand(String value){this.value=value;}


    @Override
    public byte[] getBytes() {return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }


}
