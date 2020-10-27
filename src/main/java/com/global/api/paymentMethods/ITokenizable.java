package com.global.api.paymentMethods;

public interface ITokenizable {
	
    String getToken();
    void setToken(String token);
    String tokenize();
    String tokenize(String configName);
    String tokenize(boolean validateCard);
    String tokenize(boolean validateCard, String configName);
}