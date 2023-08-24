package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

public class TokenizationData {
    @Getter @Setter
    private String token;
    @Getter @Setter
    private String merchantId;
    @Getter @Setter
    private String expiry;

    public static TokenizationData setTokenizedData(String token, String merchantId){
        TokenizationData rvalue = new TokenizationData();
        rvalue.setToken(token);
        rvalue.setMerchantId(merchantId);
        return rvalue;
    }

}
