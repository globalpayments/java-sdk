package com.global.api.paymentMethods;

import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;

public interface ITokenizable {
	
    String getToken();
    void setToken(String token);
    String tokenize();
    String tokenize(String configName);
    String tokenize(boolean validateCard);
    String tokenize(boolean validateCard, String configName);
    boolean updateTokenExpiry(String configName) throws ApiException;
    boolean deleteToken(String configName) throws ApiException;
    ITokenizable detokenize(String configName) throws ApiException;
}