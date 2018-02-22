package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IReversable {
    AuthorizationBuilder reverse();
    AuthorizationBuilder reverse(BigDecimal amount);
}
