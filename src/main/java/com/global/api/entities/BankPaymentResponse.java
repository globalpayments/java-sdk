package com.global.api.entities;

import com.global.api.entities.enums.BankPaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class BankPaymentResponse {
    @Getter @Setter private String id;
    @Getter @Setter private String redirectUrl;
    @Getter @Setter private String paymentStatus;
    @Getter @Setter private BankPaymentType type;
    @Getter @Setter private String tokenRequestId;
    @Getter @Setter private String sortCode;
    @Getter @Setter private String accountName;
    @Getter @Setter private String accountNumber;
    @Getter @Setter private String iban;
    @Getter @Setter private String remittanceReferenceValue;
    @Getter @Setter private String remittanceReferenceType;
    @Getter @Setter private BigDecimal amount;
    @Getter @Setter private String currency;
    @Getter @Setter private String maskedIbanLast4;
}