package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.AlternativePaymentType;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class AlternativePaymentMethod implements IPaymentMethod, IChargable {
    private PaymentMethodType paymentMethodType;
    private AlternativePaymentType alternativePaymentMethodType;
    private String returnUrl;
    private String cancelUrl;
    private String statusUpdateUrl;
    private String descriptor;
    private String country;
    private String accountHolderName;
    // The reference from the payment provider: from PayPal, etc
    private String providerReference;
    // Accepted values ENABLE/DISABLE
    private String addressOverrideMode;

    public AlternativePaymentMethod() {
        this.paymentMethodType = PaymentMethodType.APM;
    }

    public AlternativePaymentMethod(AlternativePaymentType alternativePaymentType) {
        this.alternativePaymentMethodType = alternativePaymentType;
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

    public AuthorizationBuilder charge(double amount) {
        return charge(new BigDecimal(amount));
    }

    public AuthorizationBuilder authorize(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Auth, this)
                        .withModifier(TransactionModifier.AlternativePaymentMethod)
                        .withAmount(amount);
    }

    public AuthorizationBuilder authorize(double amount) {
        return authorize(new BigDecimal(amount));
    }

}