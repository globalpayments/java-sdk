package com.global.api.entities;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.gateways.IRecurringGateway;
import com.global.api.services.RecurringService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class RecurringEntity<TResult extends IRecurringEntity> implements IRecurringEntity<TResult> {
    protected String id;
    protected String key;
    protected String responseCode;
    protected String responseMessage;

    private RecurringService recurringService;

    public String getKey() {
        if(key != null)
            return key;
        else return id;
    }

    protected RecurringEntity() {
        this.recurringService = new RecurringService();
    }

    public TResult create() throws ApiException {
        return create("default");
    }

    public TResult create(String configName) throws ApiException {
        return create(configName);
    }

    protected static void checkSupportsRetrieval(String configName) throws ApiException {
        IRecurringGateway client = ServicesContainer.getInstance().getRecurring(configName);
        if(!client.supportsRetrieval())
            throw new UnsupportedTransactionException();
    }
}
