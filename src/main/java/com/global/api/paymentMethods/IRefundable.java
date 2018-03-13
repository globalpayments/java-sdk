package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IRefundable {
    AuthorizationBuilder refund();
    AuthorizationBuilder refund(BigDecimal amount);
}
