package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IEditable {
    AuthorizationBuilder edit();
    AuthorizationBuilder edit(BigDecimal amount);
}
