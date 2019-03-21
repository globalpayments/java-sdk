package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;

public class DE3_ProcessingCode implements IDataElement<DE3_ProcessingCode> {
    private DE3_TransactionType transactionType;
    private DE3_AccountType fromAccount;
    private DE3_AccountType toAccount;

    public DE3_TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(DE3_TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    public DE3_AccountType getFromAccount() {
        return fromAccount;
    }
    public void setFromAccount(DE3_AccountType fromAccount) {
        this.fromAccount = fromAccount;
    }
    public DE3_AccountType getToAccount() {
        return toAccount;
    }
    public void setToAccount(DE3_AccountType toAccount) {
        this.toAccount = toAccount;
    }

    public DE3_ProcessingCode fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        transactionType = sp.readStringConstant(2, DE3_TransactionType.class);
        fromAccount = sp.readStringConstant(2, DE3_AccountType.class);
        toAccount = sp.readStringConstant(2, DE3_AccountType.class);

        return this;
    }

    public byte[] toByteArray() {
        return transactionType.getValue()
                .concat(fromAccount.getValue())
                .concat(toAccount.getValue())
                .getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
