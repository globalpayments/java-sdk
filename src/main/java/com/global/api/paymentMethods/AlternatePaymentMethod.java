package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.AlternativePaymentType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class AlternatePaymentMethod implements IPaymentMethod, IChargable {
    private PaymentMethodType paymentMethodType;
    private AlternativePaymentType alternativePaymentMethodType;
    private String returnUrl;
    private String cancelUrl;
    private String statusUpdateUrl;
    private String descriptor;
    private String country;
    private String accountHolderName;

    public AlternatePaymentMethod() {
        this.paymentMethodType = PaymentMethodType.Credit;
    }

    @Override
    public AuthorizationBuilder charge() {
        return charge(null);
    }

    @Override
    public AuthorizationBuilder charge(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Sale, this)
                        .withAmount(amount);
    }

}
