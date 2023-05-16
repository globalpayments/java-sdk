package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.enums.UsableBalanceMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
public class AccountFunds implements IPaymentMethod {
    @Getter @Setter private String accountId;
    @Getter @Setter private String AccountName;
    @Getter @Setter private String merchantId;
    @Getter @Setter private String recipientAccountId;
    @Getter @Setter private UsableBalanceMode usableBalanceMode;

    /**
     * This is needed because we are implementing IPaymentMethod interface
     * @return PaymentMethodType.Account_Funds
     */
    @Override
    public PaymentMethodType getPaymentMethodType() {
        return  PaymentMethodType.AccountFunds;
    }

    // Transfers specific funds to another merchant account
    public AuthorizationBuilder transfer(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.TransferFunds)
                        .withAmount(amount)
                        .withPaymentMethod(this);
    }
}