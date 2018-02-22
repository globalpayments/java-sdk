package com.global.api.services;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.Customer;
import com.global.api.entities.IRecurringCollection;
import com.global.api.entities.IRecurringEntity;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

import java.util.List;

public class RecurringService {
    public static <T extends IRecurringEntity> T create(T entity, Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Create, entity, clazz).execute();
    }

    public static <T extends IRecurringEntity> T delete(T entity, Class<T> clazz) throws ApiException {
        return delete(entity, clazz, false);
    }
    public static <T extends IRecurringEntity> T delete(T entity, Class<T> clazz, boolean force) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Delete, entity, clazz)
                .withForceDelete(force)
                .execute();
    }

    public static <T extends IRecurringEntity> T edit(T entity, Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Edit, entity, clazz).execute();
    }

    public static <T extends IRecurringEntity> T get(String key, Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Fetch, clazz)
                .withKey(key)
                .execute();
    }

    public static <T extends IRecurringCollection> RecurringBuilder<T> search(Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Search, clazz);
    }
}
