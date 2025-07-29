package com.global.api.terminals.builders;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.StoredCredentialInitiator;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.TerminalResponse;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public abstract class TerminalBuilder<T extends TerminalBuilder<T>> extends TransactionBuilder<TerminalResponse> {
    protected PaymentMethodType paymentMethodType;
    protected Integer requestId;
    protected Integer clerkId; // ID of the clerk in retail mode, server in restaurant mode
    protected String referenceNumber;
    protected String clerkNumber;
    protected StoredCredentialInitiator storedCredentialInitiator;

    public TerminalBuilder<T> withClerkId(Integer value) {
        clerkId = value;
        return this;
    }
    public TerminalAuthBuilder withCardOnFileIndicator(StoredCredentialInitiator value){
        storedCredentialInitiator = value;
        return (TerminalAuthBuilder) this;
    }
    public TerminalBuilder<T> withReferenceNumber(String value) {
        referenceNumber = value;
        return this;
    }
    public TerminalBuilder<T> withRequestId(Integer value) {
        requestId = value;
        return this;
    }
    public TerminalBuilder<T> withClerkNumber(String value) {
        this.clerkNumber = value;
        return this;
    }

    TerminalBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type);
        paymentMethodType = paymentType;
    }
    public TerminalBuilder withTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        return this;
    }
}
