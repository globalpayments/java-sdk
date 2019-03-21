package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.VapsConnector;

public class ResubmitBuilder extends TransactionBuilder<Transaction> {
    String transactionToken;

    public String getTransactionToken() {
        return transactionToken;
    }

    public ResubmitBuilder withTransactionToken(String token) {
        this.transactionToken = token;
        return this;
    }

    public ResubmitBuilder(TransactionType type) {
        super(type, null);
    }

    public Transaction execute(String configName) throws ApiException {
        super.execute(configName);

        IPaymentGateway client = ServicesContainer.getInstance().getGateway(configName);
        if(client instanceof VapsConnector) {
            return ((VapsConnector)client).resubmitTransaction(this);
        }
        else {
            throw new UnsupportedTransactionException("Resubmissions are not allowed for the currently configured gateway.");
        }
    }

    public void setupValidations() {

    }
}
