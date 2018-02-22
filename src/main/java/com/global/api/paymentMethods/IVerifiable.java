package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;

public interface IVerifiable {
    AuthorizationBuilder verify();
}
