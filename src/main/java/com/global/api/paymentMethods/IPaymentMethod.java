package com.global.api.paymentMethods;

import com.global.api.entities.enums.PaymentMethodType;

public interface IPaymentMethod {
    PaymentMethodType getPaymentMethodType();
}
