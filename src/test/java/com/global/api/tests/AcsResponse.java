package com.global.api.tests;

public class AcsResponse {
    private String authResponse;
    private String merchantData;

    public String getAuthResponse() {
        return authResponse;
    }
    public void setAuthResponse(String authResponse) {
        this.authResponse = authResponse;
    }
    public String getMerchantData() {
        return merchantData;
    }
    public void setMerchantData(String merchantData) {
        this.merchantData = merchantData;
    }
}
