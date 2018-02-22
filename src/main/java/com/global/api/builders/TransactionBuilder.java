package com.global.api.builders;

import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.paymentMethods.IPaymentMethod;

public abstract class TransactionBuilder<TResult> extends BaseBuilder<TResult> {
    protected TransactionType transactionType;
    protected TransactionModifier transactionModifier = TransactionModifier.None;
    protected IPaymentMethod paymentMethod;

    public TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    public TransactionModifier getTransactionModifier() {
        return transactionModifier;
    }
    public void setTransactionModifier(TransactionModifier transactionModifier) {
        this.transactionModifier = transactionModifier;
    }
    public IPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(IPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public TransactionBuilder(TransactionType type) {
        this(type, null);
    }
    public TransactionBuilder(TransactionType type, IPaymentMethod paymentMethod){
        super();
        this.transactionType = type;
        this.paymentMethod = paymentMethod;
    }
}
