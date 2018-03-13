package com.global.api.paymentMethods;

public interface ITokenizable {
    String getToken();
    void setToken(String token);

    String tokenize();
}
