package com.global.api.entities;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.IRecurringGateway;
import com.global.api.paymentMethods.RecurringPaymentMethod;
import com.global.api.services.RecurringService;

import java.util.List;

public abstract class RecurringEntity<TResult extends IRecurringEntity> implements IRecurringEntity<TResult> {
    protected String id;
    protected String key;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getKey() {
        if(key != null)
            return key;
        else return id;
    }
    public void setKey(String key) {
        this.key = key;
    }

    protected RecurringEntity() {}

    protected static void checkSupportsRetrieval() throws ApiException {
        IRecurringGateway client = ServicesContainer.getInstance().getRecurring();
        if(!client.supportsRetrieval())
            throw new UnsupportedTransactionException();
    }
}
