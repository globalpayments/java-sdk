package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Accessors(chain = true)
public class BankPayment implements IPaymentMethod, IChargable, INotificationData {

    // Merchant/Individual Name
    @Getter @Setter private String accountName;

    // Financial institution account number
    @Getter @Setter private String accountNumber;

    // A SORT Code is a number code, which is used by British and Irish banks.
    // These codes have six digits, and they are divided into three different pairs, such as 12-34-56.
    @Getter @Setter private String sortCode;

    // The International Bank Account Number
    @Getter @Setter private String iban;

    @Getter private PaymentMethodType paymentMethodType = PaymentMethodType.BankPayment;

    @Getter @Setter private String returnUrl;

    @Getter @Setter private String statusUpdateUrl;

    @Getter @Setter private String cancelUrl;

    @Getter @Setter private BankPaymentType BankPaymentType;

    @Getter @Setter private List<String> countries;

    public AuthorizationBuilder charge() {
        return charge(null);
    }

    // Mandatory request used to initiate an Open Banking transaction
    public AuthorizationBuilder charge(BigDecimal amount) {
        return
                new AuthorizationBuilder(TransactionType.Sale, this)
                        .withModifier(TransactionModifier.BankPayment)
                        .withAmount(amount);
    }

}