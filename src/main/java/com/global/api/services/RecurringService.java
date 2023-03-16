package com.global.api.services;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.IRecurringCollection;
import com.global.api.entities.IRecurringEntity;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

public class RecurringService {
    public static <T extends IRecurringEntity> T create(T entity, Class<T> clazz) throws ApiException {
        return create(entity, clazz, "default");
    }

    public static <T extends IRecurringEntity> T create(T entity, Class<T> clazz, String configName) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Create, entity, clazz).execute(configName);
    }

    public static <T extends IRecurringEntity> T delete(T entity, Class<T> clazz) throws ApiException {
        return delete(entity, clazz, false);
    }

    public static <T extends IRecurringEntity> T delete(T entity, Class<T> clazz, boolean force) throws ApiException {
        return delete(entity, clazz, false, "default");
    }

    public static <T extends IRecurringEntity> T delete(T entity, Class<T> clazz, boolean force, String configName) throws ApiException {
        return
                new RecurringBuilder<T>(TransactionType.Delete, entity, clazz)
                        .withForceDelete(force)
                        .execute(configName);
    }

    public static <T extends IRecurringEntity> T edit(T entity, Class<T> clazz) throws ApiException {
        return edit(entity, clazz, "default");
    }

    public static <T extends IRecurringEntity> T edit(T entity, Class<T> clazz, String configName) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Edit, entity, clazz).execute(configName);
    }

    public static <T extends IRecurringEntity> T get(String key, Class<T> clazz) throws ApiException {
        return get(key, clazz, "default");
    }

    public static <T extends IRecurringEntity> T get(String key, Class<T> clazz, String configName) throws ApiException {
        IRecurringEntity entity;
        try {
            entity = clazz.newInstance();
            entity.setKey(key);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }

        return
                new RecurringBuilder<T>(TransactionType.Fetch, entity, clazz)
                        .withKey(key)
                        .execute(configName);
    }

    public static <T extends IRecurringCollection> RecurringBuilder<T> search(Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Search, clazz);
    }

    public static <T extends IRecurringCollection> RecurringBuilder<T> search(IRecurringEntity entity, Class<T> clazz) throws ApiException {
        return new RecurringBuilder<T>(TransactionType.Search, entity, clazz);
    }
}
