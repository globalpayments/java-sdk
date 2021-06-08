package com.global.api.paymentMethods;

import com.global.api.entities.exceptions.ApiException;

public interface ITokenizable {
	
    String getToken();
    void setToken(String token);
    String tokenize(String configName) throws ApiException;
    String tokenize(boolean validateCard, String configName) throws ApiException;
    boolean updateTokenExpiry(String configName) throws ApiException;
    boolean deleteToken(String configName) throws ApiException;
}