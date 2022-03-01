package com.global.api.terminals.builders;

import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.TerminalResponse;

public abstract class TerminalBuilder<T extends TerminalBuilder<T>> extends TransactionBuilder<TerminalResponse> {
    protected PaymentMethodType paymentMethodType;
    protected Integer requestId;
    /*
     * ID of the clerk if in retail mode, and ID of the server if in restaurant mode
     * 
     * @var int
     */
    protected Integer clerkId;
    protected String referenceNumber;

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    public Integer getRequestId() {
        return requestId;
    }
    public Integer getClerkId() {
        return this.clerkId;
    }
    public String getReferenceNumber() { return this.referenceNumber; }

    public TerminalBuilder<T> withClerkId(Integer value) {
        clerkId = value;
        return this;
    }

    public TerminalBuilder<T> withReferenceNumber(String value) {
        referenceNumber = value;
        return this;
    }

    public TerminalBuilder<T> withRequestId(Integer value) {
        requestId = value;
        return this;
    }

    TerminalBuilder(TransactionType type, PaymentMethodType paymentType) {
        super(type);
        paymentMethodType = paymentType;
    }
}
