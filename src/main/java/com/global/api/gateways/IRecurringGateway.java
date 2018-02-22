package com.global.api.gateways;

import com.global.api.builders.RecurringBuilder;
import com.global.api.entities.exceptions.ApiException;

public interface IRecurringGateway {
    boolean supportsRetrieval();
    boolean supportsUpdatePaymentDetails();
    <T> T processRecurring(RecurringBuilder<T> builder, Class<T> clazz) throws ApiException;
}
