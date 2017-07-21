package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IPrePayable {
    AuthorizationBuilder addValue();
    AuthorizationBuilder addValue(BigDecimal amount);
}
