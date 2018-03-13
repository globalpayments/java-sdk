package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IAuthable {
    AuthorizationBuilder authorize();
    AuthorizationBuilder authorize(BigDecimal amount);
}
