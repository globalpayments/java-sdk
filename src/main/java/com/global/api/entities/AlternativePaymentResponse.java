package com.global.api.entities;

import com.global.api.BankResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;

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
    private String providerReference;
    private String providerName;
    private String ack;
    private String sessionToken;
    private String correlationReference;
    private String versionReference;
    private String buildReference;
    private DateTime timeCreatedReference;
    private String transactionReference;
    private String secureAccountReference;
    private String reasonCode;
    private String pendingReason;
    private BigDecimal grossAmount;
    private DateTime paymentTimeReference;
    private String paymentType;
    private String paymentStatus;
    private String type;
    private String protectionEligibility;
    private String authStatus;
    private BigDecimal authAmount;
    private String authAck;
    private String authCorrelationReference;
    private String authVersionReference;
    private String authBuildReference;
    private String authPendingReason;
    private String authProtectionEligibility;
    private String authProtectionEligibilityType;
    private String authReference;
    private BigDecimal feeAmount;
    private BankResponse bank;
}