package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class AlternativePaymentResponse {
    // Bank account details
    private String bankAccount;

    // Account holder name of the customer’s account
    private String accountHolderName;

    // 2 character ISO country code
    private String country;

    // URL to redirect the customer to - only available in PENDING asynchronous transactions.
    // Sent there so merchant can redirect consumer to complete an interrupted payment.
    private String redirectUrl;

    // Reflects what the customer will see on the proof of payment
    // For example: bank statement record and similar. Also known as the payment descriptor
    private String paymentPurpose;

    // Payment method
    private String paymentMethod;
}