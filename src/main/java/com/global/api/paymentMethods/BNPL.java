package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.BNPLType;
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
public class BNPL implements IPaymentMethod, IAuthable, INotificationData {

    private PaymentMethodType paymentMethodType;

    @Override
    public PaymentMethodType getPaymentMethodType() {
        return PaymentMethodType.BNPL;
    }

    public BNPLType BNPLType;

    // The endpoint to which the customer should be redirected after a payment has been attempted or
    // successfully completed on the payment scheme's site.
    public String returnUrl;

    // The endpoint which will receive payment-status messages.
    // This will include the result of the transaction or any updates to the transaction status.
    // For certain asynchronous payment methods these notifications may come hours or
    // days after the initial authorization.
    public String statusUpdateUrl;

    // The customer will be redirected back to your notifications.cancel_url in case the transaction is canceled
    public String cancelUrl;

    @Override
    public AuthorizationBuilder authorize() {
        return authorize(null, false);
    }

    @Override
    public AuthorizationBuilder authorize(BigDecimal amount) {
        return authorize(amount, false);
    }

    @Override
    public AuthorizationBuilder authorize(BigDecimal amount, boolean isEstimated) {
        return
                new AuthorizationBuilder(TransactionType.Auth, this)
                        .withModifier(TransactionModifier.BuyNowPayLater)
                        .withAmount(amount);
    }

    public AuthorizationBuilder authorize(double amount) {
        return authorize(new BigDecimal(amount));
    }

}