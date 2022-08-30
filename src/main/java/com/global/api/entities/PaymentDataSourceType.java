package com.global.api.entities;

import com.global.api.entities.enums.IStringConstant;

public enum PaymentDataSourceType implements IStringConstant {
     APPLEPAY("ApplePay"),
     APPLEPAYAPP("ApplePayApp"),
     APPLEPAYWEB("ApplePayWeb"),
     GOOGLEPAYAPP("GooglePayApp"),
     GOOGLEPAYWEB("GooglePayWeb"),
     DISCOVER3DSECURE("Discover 3DSecure");

     final String value;

     PaymentDataSourceType(String value){
         this.value = value;
     }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
