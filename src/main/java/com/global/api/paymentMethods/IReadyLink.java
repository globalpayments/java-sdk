package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

import java.math.BigDecimal;

public interface IReadyLink {
    AuthorizationBuilder load();
    AuthorizationBuilder load(BigDecimal amount);
    AuthorizationBuilder loadReversal();
    AuthorizationBuilder loadReversal(BigDecimal amount);
}
