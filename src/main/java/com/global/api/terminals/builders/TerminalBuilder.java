package com.global.api.terminals.builders;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.ITerminalResponse;

public abstract class TerminalBuilder<T extends TerminalBuilder<T>> extends TransactionBuilder<ITerminalResponse> {
    protected PaymentMethodType paymentMethodType;
    protected Integer requestId;
    protected Integer referenceNumber;

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    public Integer getRequestId() {
        return requestId;
    }
    
    public Integer getReferenceNumber() {
    	return referenceNumber;
    }

    public T withRequestId(Integer value) {
        requestId = value;
        return (T)this;
    }
    
    public T withReferenceNumber(Integer value) {
    	referenceNumber = value;
    	return (T)this;
    }

    TerminalBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type);
        paymentMethodType = paymentType;
    }
}
