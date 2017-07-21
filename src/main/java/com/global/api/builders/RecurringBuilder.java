package com.global.api.builders;

import com.global.api.ServicesContainer;
import com.global.api.entities.Customer;
import com.global.api.entities.IRecurringEntity;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.gateways.IRecurringGateway;

import java.util.EnumSet;
import java.util.HashMap;

public class RecurringBuilder<TResult> extends TransactionBuilder<TResult> {
    private String key;
    private String orderId;
    private IRecurringEntity entity;
    private HashMap<String, String> searchCriteria = new HashMap<String, String>();
    private Class<TResult> clazz;
    private boolean forceDelete = false;

    public String getKey() {
        return key;
    }
    public String getOrderId() {
        return orderId;
    }
    public IRecurringEntity getEntity() {
        return entity;
    }
    public HashMap<String, String> getSearchCriteria() {
        return searchCriteria;
    }
    public boolean isForceDelete() {
        return forceDelete;
    }

    public RecurringBuilder<TResult> addSearchCriteria(String key, String value) {
        searchCriteria.put(key, value);
        return this;
    }
    public RecurringBuilder<TResult> withKey(String value) {
        this.key = value;
        return this;
    }
    public RecurringBuilder<TResult> withForceDelete(boolean value) {
        this.forceDelete = value;
        return this;
    }

    public RecurringBuilder(TransactionType type) {
        super(type);
    }
    public RecurringBuilder(TransactionType type, Class<TResult> clazz) {
        super(type);
        this.clazz = clazz;
    }
    public RecurringBuilder(TransactionType type, IRecurringEntity entity, Class<TResult> clazz) {
        super(type);

        if(entity != null) {
            this.entity = entity;
            this.key = entity.getKey();
            this.clazz = clazz;
        }
    }

    public TResult execute() throws ApiException {
        super.execute();

        IRecurringGateway client = ServicesContainer.getInstance().getRecurring();
        return client.processRecurring(this, clazz);
    }

    public void setupValidations() {
        this.validations.of(EnumSet.of(TransactionType.Edit, TransactionType.Delete, TransactionType.Fetch))
                .check("key").isNotNull();
        this.validations.of(TransactionType.Search).check("searchCriteria").isNotNull();
    }
}
