package com.global.api.services;

import com.global.api.builders.InstallmentBuilder;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.Installment;

public class InstallmentService {

    /**
     * Creates the Installment
     * @param entity
     * @param configName
     * @return {@link Installment}
     * @throws ApiException
     */
    public static Installment create(Installment entity, String configName) throws ApiException {
        return new InstallmentBuilder(entity) {
            @Override
            public void setupValidations() {

            }
        }.execute(configName);
    }
}
