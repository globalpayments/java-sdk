package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IChargable {
    AuthorizationBuilder charge();
    AuthorizationBuilder charge(BigDecimal amount);
}
