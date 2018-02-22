package com.global.api.terminals.builders;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.TerminalResponse;

public abstract class TerminalBuilder<T extends TerminalBuilder<T>> extends TransactionBuilder<TerminalResponse> {
    protected PaymentMethodType paymentMethodType;
    protected Integer referenceNumber;

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    public Integer getReferenceNumber() {
        return referenceNumber;
    }

    public T withReferenceNumber(Integer value) {
        this.referenceNumber = value;
        return (T)this;
    }

    TerminalBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type, null);
        paymentMethodType = paymentType;
    }
}
