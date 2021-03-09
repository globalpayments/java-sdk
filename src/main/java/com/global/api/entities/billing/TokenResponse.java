package com.global.api.entities.billing;

public class TokenResponse extends BillingResponse {
    protected String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
