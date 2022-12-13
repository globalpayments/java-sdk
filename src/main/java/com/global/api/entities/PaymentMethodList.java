package com.global.api.entities;

import com.global.api.entities.enums.PaymentMethodFunction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class PaymentMethodList {
    public PaymentMethodFunction function;
    public Object paymentMethod;
}