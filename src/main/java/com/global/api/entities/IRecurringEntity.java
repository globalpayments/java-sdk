package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;

public interface IRecurringEntity<T> {
    String getId();
    void setId(String value);
    String getKey();
    void setKey(String value);

    T create(String configName) throws ApiException;
    void delete() throws ApiException;
    void delete(boolean force) throws ApiException;
    T saveChanges() throws ApiException;
}
