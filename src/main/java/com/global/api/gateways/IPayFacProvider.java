package com.global.api.gateways;

import com.global.api.builders.PayFacBuilder;
import com.global.api.entities.exceptions.ApiException;

public interface IPayFacProvider {
    // <T> T processPayFac(PayFacBuilder<T> builder, Class<T> clazz) throws ApiException;
    <T> T processPayFac(PayFacBuilder<T> builder) throws ApiException;
    // <T> T  processBoardingUser(PayFacBuilder<T> builder, Class<T> clazz) throws ApiException;
    <T> T  processBoardingUser(PayFacBuilder<T> builder) throws ApiException;
}