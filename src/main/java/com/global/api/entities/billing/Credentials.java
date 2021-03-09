package com.global.api.entities.billing;

public class Credentials {
    protected String apiKey;
    protected String merchantName;
    protected String password;
    protected String userName;

    public String getApiKey() {
        return apiKey;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
